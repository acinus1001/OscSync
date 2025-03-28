package dev.kuro9.application.discord.advice

import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.error.handler.discord.DiscordErrorEvent
import dev.kuro9.internal.error.handler.ServerErrorEvent
import dev.kuro9.internal.error.handler.ServerErrorHandler
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.springframework.stereotype.Component

@Component
class DiscordErrorResponseAdvice : ServerErrorHandler {
    val log by useLogger()

    override suspend fun doHandle(event: ServerErrorEvent) {
        if (event !is DiscordErrorEvent) return

        try {
            when (event.discordEvent) {
                is SlashCommandInteractionEvent -> handleSlashError(
                    event.t,
                    event.discordEvent as SlashCommandInteractionEvent
                )

                is CommandAutoCompleteInteractionEvent -> {}
                is ButtonInteractionEvent -> {}
            }
        } catch (e: Throwable) {
            log.error("Error occurred while handling event", e)
        }
    }

    private suspend fun handleSlashError(t: Throwable, event: SlashCommandInteractionEvent) {
        log.error("error on slash command", t)
        Embed {
            title = "Unexpected Error Occurred"
            description = "Please contact to <@!400579163959853056>"
            field {
                name = "Author"
                value = event.user.asMention
                inline = true
            }
            field {
                name = "Command"
                value = event.fullCommandName
                inline = true
            }
            field {
                name = "RequestTime"
                value = event.timeCreated.toString()
                inline = true
            }
        }.let {
            event.channel.sendMessageEmbeds(it).await()
        }
    }
}