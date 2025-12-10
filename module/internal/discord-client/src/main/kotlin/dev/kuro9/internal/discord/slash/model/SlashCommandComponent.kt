package dev.kuro9.internal.discord.slash.model

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommandComponent {
    val commandData: SlashCommandData

    suspend fun handleEvent(event: SlashCommandInteractionEvent)
    suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
    suspend fun handleButtonEvent(event: ButtonInteractionEvent) {}
    suspend fun handleModalEvent(event: ModalInteractionEvent) {}
}


