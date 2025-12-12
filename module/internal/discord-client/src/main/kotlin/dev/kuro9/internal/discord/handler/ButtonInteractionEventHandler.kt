package dev.kuro9.internal.discord.handler

import dev.kuro9.internal.discord.handler.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.model.DiscordEventHandler
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(ButtonInteractionHandler::class)
class ButtonInteractionEventHandler(
    private val handler: List<ButtonInteractionHandler>,
    private val eventPublisher: ApplicationEventPublisher,
) : DiscordEventHandler<ButtonInteractionEvent> {

    override val kClass = ButtonInteractionEvent::class
    override suspend fun handle(event: ButtonInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch(CoroutineExceptionHandler { _, e ->
            error(e) { "event publisher error" }
        }) {
            eventPublisher.publishEvent(event)
        }

        handler.filter { it.isHandleable(event) }
            .forEach { it.handleButtonInteraction(event) }
    }
}