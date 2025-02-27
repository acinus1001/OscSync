package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class SlashCommandListener(
    slashCommands: List<SlashCommandComponent>
) : DiscordEventHandler<SlashCommandInteractionEvent> {
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandId }

    override val kClass = SlashCommandInteractionEvent::class

    override fun handle(event: SlashCommandInteractionEvent) {
        commandMap[event.commandId]?.handleEvent(event) ?: errorLog("No such command: ${event.commandId}")
    }
}