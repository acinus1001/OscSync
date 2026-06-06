package dev.kuro9.internal.discord.handler

import dev.kuro9.internal.discord.handler.model.EntitySelectInteractionHandler
import dev.kuro9.internal.discord.model.DiscordEventHandler
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(EntitySelectInteractionHandler::class)
class EntitySelectInteractionEventHandler(
    private val handler: List<EntitySelectInteractionHandler>,
    private val eventPublisher: ApplicationEventPublisher,
) : DiscordEventHandler<EntitySelectInteractionEvent> {
    override val kClass = EntitySelectInteractionEvent::class
    override suspend fun handle(event: EntitySelectInteractionEvent) {
        CoroutineScope(Dispatchers.IO).launch(CoroutineExceptionHandler { _, e ->
            error(e) { "event publisher error" }
        }) { eventPublisher.publishEvent(event) }

        handler.filter { it.isHandleable(event) }.forEach { it.handleEntitySelectInteraction(event) }
    }
}