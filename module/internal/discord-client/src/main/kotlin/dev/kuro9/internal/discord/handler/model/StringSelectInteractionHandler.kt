package dev.kuro9.internal.discord.handler.model

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

interface StringSelectInteractionHandler {
    suspend fun isHandleable(event: StringSelectInteractionEvent): Boolean
    suspend fun handleStringSelectInteraction(event: StringSelectInteractionEvent)
}