package dev.kuro9.internal.discord.handler.model

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

interface ModalInteractionHandler {
    suspend fun isHandleable(event: ModalInteractionEvent): Boolean
    suspend fun handleModalInteraction(event: ModalInteractionEvent)
}