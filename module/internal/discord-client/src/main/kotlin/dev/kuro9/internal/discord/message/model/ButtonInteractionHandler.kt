package dev.kuro9.internal.discord.message.model

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface ButtonInteractionHandler {
    suspend fun isHandleable(event: ButtonInteractionEvent): Boolean
    suspend fun handleButtonInteraction(event: ButtonInteractionEvent)
}