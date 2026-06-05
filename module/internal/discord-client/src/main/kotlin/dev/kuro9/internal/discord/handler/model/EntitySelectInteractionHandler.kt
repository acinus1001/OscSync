package dev.kuro9.internal.discord.handler.model

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent

interface EntitySelectInteractionHandler {
    suspend fun isHandleable(event: EntitySelectInteractionEvent): Boolean
    suspend fun handleEntitySelectInteraction(event: EntitySelectInteractionEvent)
}