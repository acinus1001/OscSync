package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

@Component
class DiscordPingCommand : SlashCommandComponent {
    override val commandId: String = "ping"
    override val commandType: String
        get() = TODO("Not yet implemented")
    override val commandDescription: String = "ping time"
    override val isEphemeral: Boolean = true

    override fun handleEvent(event: SlashCommandInteractionEvent) {
        val requestTime = event.timeCreated.toInstant()
        val duration = Duration.between(requestTime, Instant.now()).toKotlinDuration()
        event.reply(duration.toString()).queue()
    }
}