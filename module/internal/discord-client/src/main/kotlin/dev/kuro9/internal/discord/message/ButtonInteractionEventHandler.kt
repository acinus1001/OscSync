package dev.kuro9.internal.discord.message

import dev.kuro9.internal.discord.message.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(ButtonInteractionHandler::class)
class ButtonInteractionEventHandler(
    private val handler: List<ButtonInteractionHandler>,
) : DiscordEventHandler<ButtonInteractionEvent> {

    override val kClass = ButtonInteractionEvent::class
    override suspend fun handle(event: ButtonInteractionEvent) {

        handler.filter { it.isHandleable(event) }
            .forEach { it.handleButtonInteraction(event) }
    }
}