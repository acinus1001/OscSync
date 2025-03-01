package dev.kuro9.internal.discord.slash.model

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommandComponent {
    val commandData: SlashCommandData

    fun handleEvent(event: SlashCommandInteractionEvent)
}


