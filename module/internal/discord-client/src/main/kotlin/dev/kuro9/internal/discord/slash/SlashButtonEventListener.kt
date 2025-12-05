package dev.kuro9.internal.discord.slash

import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
internal class SlashButtonEventListener(
    slashCommands: List<SlashCommandComponent>,
    private val eventPublisher: ApplicationEventPublisher
) : DiscordEventHandler<ButtonInteractionEvent> {
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }
    override val kClass = ButtonInteractionEvent::class

    override suspend fun handle(event: ButtonInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch(CoroutineExceptionHandler { _, e ->
            error(e) { "event publisher error" }
        }) {
            eventPublisher.publishEvent(event)
        }
        commandMap[event.componentId]?.handleButtonEvent(event)
    }
}