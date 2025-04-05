package dev.kuro9.application.discord.slash

import dev.kuro9.common.exception.DuplicatedInsertException
import dev.kuro9.domain.karaoke.service.KaraokeApiService
import dev.kuro9.domain.karaoke.service.KaraokeChannelService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashKaraokeCommand(
    private val channelService: KaraokeChannelService,
    private val apiService: KaraokeApiService
) : SlashCommandComponent {
    override val commandData = Command("kara", "노래방 관련 명령어 모음") {
        group("channel", "알림 관련 채널 설정") {
            restrict(guild = true) // guild only
            subcommand("register", "이 채널을 신곡 알림 받을 채널로 등록합니다.") {
                option<TextChannel>("channel", "등록할 채널 (기본값=현재 채널)")
            }
            subcommand("unregister", "이 채널을 등록 해제합니다.")
        }

        group("search", "노래 검색") {
            subcommand("no", "곡 번호로 검색합니다.") {
                option<Int>(
                    "song-number",
                    "노래 번호",
                    required = true,
                    autocomplete = false
                )
            }
            subcommand("title", "곡 제목으로 검색합니다.") {
                option<String>(
                    "song-title",
                    "노래 제목",
                    required = true,
                    autocomplete = false
                )
            }
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = CoroutineScope(Dispatchers.IO).async {
            event.deferReply().await()
        }

        runCatching {
            when (event.subcommandGroup) {
                "channel" -> when (event.subcommandName) {
                    "register" -> registerChannel(event, deferReply)
                    "unregister" -> registerChannel(event, deferReply)
                }

                "search" -> when (event.subcommandName) {
                    "no" -> registerChannel(event, deferReply)
                    "title" -> registerChannel(event, deferReply)
                }
            }

            throw NotImplementedError("Unknown command=${event.fullCommandName}")
        }.onFailure { t: Throwable ->
            deferReply.await()
                .editOriginalEmbeds(getDefaultExceptionEmbed(t))
                .await()
        }
    }

    private suspend fun registerChannel(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val targetChannel: TextChannel =
            event.getOption("channel")?.asChannel?.asTextChannel() ?: event.channel.asTextChannel()

        val isRegistered = channelService.checkRegisteredChannel(targetChannel.idLong)

        if (isRegistered) {
            Embed {
                title = "409 Conflict"
                description = "이미 등록된 채널입니다."
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }

            return
        }

        val webhook = targetChannel.createWebhook("KGB: Karaoke Notify Hook").await()

        channelService.registerChannel(
            channelId = event.channelIdLong,
            guildId = event.guild?.idLong,
            webhookUrl = webhook.url,
            registerUserId = event.user.idLong,
        )
        
        Embed {
            title = "200 OK"
            description = "채널 등록에 성공하였습니다."
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun unregisterChannel(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {}

    private suspend fun searchByNo(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {}

    private suspend fun searchByTitle(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {}

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed = when (t) {
        is DuplicatedInsertException -> Embed {
            title = "409 Conflict"
            description = "기존 데이터와 충돌하여 저장에 실패하였습니다."
            color = Color.RED.rgb
        }

        is NotImplementedError -> Embed {
            title = "Not Implemented"
            description = "This command is not implemented. Contact <@400579163959853056> to report."
            color = Color.RED.rgb
        }

        else -> throw t
    }
}