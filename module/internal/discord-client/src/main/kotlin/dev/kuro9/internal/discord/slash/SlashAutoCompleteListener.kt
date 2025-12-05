package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
internal class SlashAutoCompleteListener(
    slashCommands: List<SlashCommandComponent>,
    private val eventPublisher: ApplicationEventPublisher
) : DiscordEventHandler<CommandAutoCompleteInteractionEvent> {
    override val kClass = CommandAutoCompleteInteractionEvent::class
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }

    override suspend fun handle(event: CommandAutoCompleteInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch(CoroutineExceptionHandler { _, e ->
            error(e) { "event publisher error" }
        }) {
            eventPublisher.publishEvent(event)
        }
        commandMap[event.name]?.handleAutoComplete(event) ?: errorLog("No such command: ${event.fullCommandName}")
    }
}