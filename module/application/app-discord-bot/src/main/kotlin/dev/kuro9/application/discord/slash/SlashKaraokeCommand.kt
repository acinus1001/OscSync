package dev.kuro9.application.discord.slash

import dev.kuro9.common.exception.DuplicatedInsertException
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.service.KaraokeApiService
import dev.kuro9.domain.karaoke.service.KaraokeChannelService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
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
            subcommand("unregister", "이 채널을 등록 해제합니다.") {
                option<TextChannel>("channel", "등록 해제할 채널 (기본값=현재 채널)")
            }
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
            subcommand("singer", "가수 이름으로 검색합니다.") {
                option<String>(
                    "singer",
                    "가수/밴드 이름",
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
                    "register" -> return registerChannel(event, deferReply)
                    "unregister" -> return unregisterChannel(event, deferReply)
                }

                "search" -> when (event.subcommandName) {
                    "no" -> return searchByNo(event, deferReply)
                    "title" -> return searchByTitle(event, deferReply)
                    "singer" -> return searchBySinger(event, deferReply)
                }
            }

            throw NotImplementedError("Unknown command=${event.fullCommandName}")
        }.onFailure { t: Throwable ->
            error(t) { "handl event error: ${event.fullCommandName}" }
            deferReply.await()
                .editOriginalEmbeds(getDefaultExceptionEmbed(t))
                .await()
        }
    }

    private suspend fun registerChannel(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {

        val targetChannel: TextChannel =
            event.getOption("channel")?.asChannel?.let {
                if (it !is TextChannel) {
                    Embed {
                        title = "400 Bad Request"
                        description = "해당 채널은 텍스트 채널이 아닙니다. 텍스트 채널을 선택해주세요."
                        color = Color.RED.rgb
                    }.let { deferReply.await().editOriginalEmbeds(it).await() }
                    return
                }

                it as TextChannel
            }
                ?: event.channel.asTextChannel()

        val isRegistered = channelService.getRegisteredChannel(targetChannel.idLong) != null

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
            webhookId = webhook.idLong,
            registerUserId = event.user.idLong,
        )

        Embed {
            title = "200 OK"
            description = "채널 등록에 성공하였습니다."
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun unregisterChannel(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val targetChannel: TextChannel =
            event.getOption("channel")?.asChannel?.let {
                if (it !is TextChannel) {
                    Embed {
                        title = "400 Bad Request"
                        description = "해당 채널은 텍스트 채널이 아닙니다. 텍스트 채널을 선택해주세요."
                        color = Color.RED.rgb
                    }.let { deferReply.await().editOriginalEmbeds(it).await() }
                    return
                }

                it as TextChannel
            } ?: event.channel.asTextChannel()

        val registerInfo = channelService.getRegisteredChannel(targetChannel.idLong)

        if (registerInfo == null) {
            Embed {
                title = "409 Conflict"
                description = "이미 등록 해제되거나 등록되지 않은 채널입니다."
                color = Color.YELLOW.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }

            return
        }

        targetChannel.deleteWebhookById(registerInfo.webhookId.toString()).await()
        channelService.unregisterChannel(registerInfo.channelId.value).takeIf { it }
            ?: throw IllegalStateException("Channel delete failed")

        Embed {
            title = "200 OK"
            description = "채널 등록 해제에 성공하였습니다."
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun searchByNo(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val songNo = event.getOption("song-number")!!.asInt

        val result = apiService.getSongInfoByNo(KaraokeBrand.TJ, songNo)

        if (result == null) {
            Embed {
                title = "404 Not Found"
                description = "해당 번호의 곡이 존재하지 않습니다."
                color = Color.YELLOW.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return
        }

        Embed {
            title = "200 OK"
            description = "TJ NO.$songNo 검색 결과"
            field {
                name = result.title
                value = result.singer
                inline = false
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun searchByTitle(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val songTitle = event.getOption("song-title")!!.asString

        val result = apiService.getSongInfoByName(KaraokeBrand.TJ, songTitle)

        Embed {
            title = "200 OK"
            description = "해당 제목에 대한 결과 : ${result.size}개 (25개까지 표시)"

            result.take(25).forEach {
                field {
                    name = "[${it.songNo}] ${it.title}"
                    value = it.singer
                    inline = false
                }
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun searchBySinger(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val singer = event.getOption("singer")!!.asString

        val result = apiService.getSongInfoByArtist(KaraokeBrand.TJ, singer)

        Embed {
            title = "200 OK"
            description = "해당 제목에 대한 결과 : ${result.size}개 (25개까지 표시)"

            result.take(25).forEach {
                field {
                    name = "[${it.songNo}] ${it.title}"
                    value = it.singer
                    inline = false
                }
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

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