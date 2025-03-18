package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.springframework.stereotype.Component

@Component
class SlashButtonEventListener(
    slashCommands: List<SlashCommandComponent>
) : DiscordEventHandler<ButtonInteractionEvent> {
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }
    override val kClass = ButtonInteractionEvent::class

    override suspend fun handle(event: ButtonInteractionEvent) {
        commandMap[event.componentId]?.handleButtonEvent(event) ?: errorLog("No such command: ${event.componentId}")
    }
}