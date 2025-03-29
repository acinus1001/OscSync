package dev.kuro9.application.discord.advice

import dev.kuro9.application.discord.config.ErrorWebhookConfig.ErrorWebhookUrl
import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.error.handler.discord.DiscordErrorEvent
import dev.kuro9.internal.error.handler.ServerErrorEvent
import dev.kuro9.internal.error.handler.ServerErrorHandler
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.minn.jda.ktx.messages.Embed
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.intellij.lang.annotations.Language
import org.springframework.stereotype.Component

@Component
class DiscordErrorWebhookAdvice(private val errorUrl: ErrorWebhookUrl) : ServerErrorHandler {
    private val log by useLogger()

    private val client = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
    }

    override suspend fun doHandle(event: ServerErrorEvent) {
        if (event !is DiscordErrorEvent) return
        val (t, discordEvent) = event

        val errorEmbed = Embed {
            title = "Exception Occurred"
            description = discordEvent.getEventCommand()
            color = 0xFF0000
            field {
                name = "EndUser"
                value = discordEvent.getAuthor()
                inline = false
            }

            field {
                name = "Exception"
                value = "`${t::class.qualifiedName}`"
                inline = false
            }

            field {
                name = "Method"
                value = "`${t.stackTrace[0].methodName}(${t.stackTrace[0].fileName}:${t.stackTrace[0].lineNumber})`"
                inline = false
            }

            field {
                name = "StackTrace"
                value = "```${t.stackTraceToString().take(1000)}```"
                inline = false
            }
        }

        val embedString = errorEmbed.toData().toJson().toString(Charsets.UTF_8)
        @Language("JSON") val bodyString =
            """{"contents":null,"embeds":[$embedString],"username":"Server Error","attachments":[]}"""
        val response =
            client.post(errorUrl.url) {
                setBody(bodyString)
                contentType(ContentType.Application.Json)
            }

        log.info("Discord Responded with ${response.status}")
    }

    private fun GenericEvent.getEventCommand() = when (this) {
        is SlashCommandInteractionEvent -> "/$fullCommandName"
        is CommandAutoCompleteInteractionEvent -> "/$fullCommandName"
        is ButtonInteractionEvent -> componentId
        else -> "<unknown>"
    }.let { "${this::class.simpleName}(`$it`)" }

    private fun GenericEvent.getAuthor() = when (this) {
        is SlashCommandInteractionEvent -> user.asMention
        is CommandAutoCompleteInteractionEvent -> user.asMention
        is ButtonInteractionEvent -> user.asMention
        else -> "<unknown>"
    }
}