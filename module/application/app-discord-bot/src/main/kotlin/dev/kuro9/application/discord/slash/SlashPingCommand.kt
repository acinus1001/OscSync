package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

@Component
class SlashPingCommand : SlashCommandComponent {
    override val commandData: SlashCommandData = slash("ping", "ping time")
        

    override fun handleEvent(event: SlashCommandInteractionEvent) {
        val requestTime = event.timeCreated.toInstant()
        val duration = Duration.between(requestTime, Instant.now()).toKotlinDuration()
        event.reply(duration.toString()).setEphemeral(true).queue()
    }
}