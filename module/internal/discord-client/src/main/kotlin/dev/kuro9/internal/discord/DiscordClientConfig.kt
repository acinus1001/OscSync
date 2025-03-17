package dev.kuro9.internal.discord

import dev.kuro9.internal.discord.model.DiscordClientProperty
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfig {

    @Bean
    fun getCoroutineDiscordListener(
        readyHandler: DiscordEventHandler<ReadyEvent>?,
        shutdownHandler: DiscordEventHandler<ShutdownEvent>?,
        slashCommandHandler: DiscordEventHandler<SlashCommandInteractionEvent>?,
        autoCompleteHandler: DiscordEventHandler<CommandAutoCompleteInteractionEvent>?,
    ): CoroutineEventListener = CoroutineEventListener { event: GenericEvent ->
        when (event) {
            is ReadyEvent -> readyHandler?.handle(event)
            is ShutdownEvent -> shutdownHandler?.handle(event)
            is SlashCommandInteractionEvent -> slashCommandHandler?.handle(event)
            is CommandAutoCompleteInteractionEvent -> autoCompleteHandler?.handle(event)
        }
    }

    @Bean
    fun getDiscordClient(
        discordProperty: DiscordClientProperty,
        eventListener: CoroutineEventListener,
        eventHandler: List<DiscordEventHandler<*>>,
    ): JDA {
        return light(discordProperty.token, enableCoroutines = true).apply {
            addEventListener(eventListener)
            awaitReady()
            eventHandler.forEach {
                it.initialize(this)
            }
        }
    }
}