package dev.kuro9.internal.discord.slash.model

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface SlashCommandComponent {
    val commandId: String
    val commandType: String
    val commandDescription: String

    val isEphemeral: Boolean

    fun handleEvent(event: SlashCommandInteractionEvent)
}


