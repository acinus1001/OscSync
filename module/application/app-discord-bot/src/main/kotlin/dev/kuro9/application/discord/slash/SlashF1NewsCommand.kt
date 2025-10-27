package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.util.checkPermission
import dev.kuro9.domain.error.handler.discord.exception.DiscordEmbedException
import dev.kuro9.domain.f1.service.F1NewsService
import dev.kuro9.domain.webhook.enums.WebhookDomainType
import dev.kuro9.domain.webhook.service.WebhookManageService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashF1NewsCommand(
    private val f1NewsService: F1NewsService,
    private val webhookService: WebhookManageService,
) : SlashCommandComponent {

    override val commandData = Command("f1", "F1 관련 기능입니다.") {
        group("news", "F1 News 관련 기능입니다.") {
//            subcommand("list", "저장된 F1 뉴스 목록을 출력합니다.")
            subcommand("register", "이 채널을 F1 뉴스 알림을 받을 채널로 등록합니다.") {
                option<TextChannel>("channel", "등록할 채널 (기본값=현재 채널)")
            }
            subcommand("unregister", "등록된 F1 뉴스 알림을 해제합니다.") {
                option<TextChannel>("channel", "등록 해제할 채널 (기본값=현재 채널)")
            }
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = CoroutineScope(Dispatchers.IO).async {
            event.deferReply().await()
        }

        runCatching {
            when (event.subcommandGroup) {
                "news" -> when (event.subcommandName) {
//                    "list" -> Unit
                    "register" -> registerChannel(event, deferReply)
                    "unregister" -> unregisterChannel(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t: Throwable ->
            error(t) { "handle event error: ${event.fullCommandName}" }
            deferReply.await()
                .editOriginalEmbeds(getDefaultExceptionEmbed(t))
                .await()
        }
    }

    private suspend fun registerChannel(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        checkPermission(event, Permission.MANAGE_WEBHOOKS)
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

                it
            }
                ?: event.channel.asTextChannel()

        val isRegistered = webhookService.getRegisteredChannel(
            domainType = WebhookDomainType.F1_NEWS,
            channelId = targetChannel.idLong
        ) != null

        if (isRegistered) {
            Embed {
                title = "409 Conflict"
                description = "이미 등록된 채널입니다."
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }

            return
        }

        val webhook = targetChannel.createWebhook("KGB : F1 News Webhook Service").await()
        webhookService.registerChannel(
            domainType = WebhookDomainType.F1_NEWS,
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
        checkPermission(event, Permission.MANAGE_WEBHOOKS)
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

                it
            } ?: event.channel.asTextChannel()

        val registerInfo = webhookService.getRegisteredChannel(
            domainType = WebhookDomainType.F1_NEWS,
            channelId = targetChannel.idLong
        )

        if (registerInfo == null) {
            Embed {
                title = "409 Conflict"
                description = "이미 등록 해제되거나 등록되지 않은 채널입니다."
                color = Color.YELLOW.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }

            return
        }

        targetChannel.deleteWebhookById(registerInfo.webhookId.toString()).await()
        webhookService.unregisterChannel(
            domainType = WebhookDomainType.F1_NEWS,
            channelId = event.channelIdLong,
        )

        Embed {
            title = "200 OK"
            description = "채널 등록 해제에 성공하였습니다."
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed = when (t) {

        is NotImplementedError -> Embed {
            title = "Not Implemented"
            description = "This command is not implemented. Contact <@400579163959853056> to report."
            color = Color.RED.rgb
        }

        is DiscordEmbedException -> t.embed

        else -> throw t
    }
}