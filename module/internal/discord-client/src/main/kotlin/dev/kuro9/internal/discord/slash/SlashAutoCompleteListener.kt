package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.springframework.stereotype.Component

@Component
internal class SlashAutoCompleteListener(
    slashCommands: List<SlashCommandComponent>
) : DiscordEventHandler<CommandAutoCompleteInteractionEvent> {
    override val kClass = CommandAutoCompleteInteractionEvent::class
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }

    override suspend fun handle(event: CommandAutoCompleteInteractionEvent) {
        commandMap[event.name]?.handleAutoComplete(event) ?: errorLog("No such command: ${event.fullCommandName}")
    }
}