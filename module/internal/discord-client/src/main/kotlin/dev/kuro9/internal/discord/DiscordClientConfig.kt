package dev.kuro9.internal.discord

import dev.kuro9.internal.discord.model.DiscordClientProperty
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfig {

    @Bean
    @Suppress("UNCHECKED_CAST")
    fun getDiscordListener(
        eventHandler: List<DiscordEventHandler<*>>
    ): ListenerAdapter = object : ListenerAdapter() {
        val handlerMap = eventHandler.associateBy { it.kClass }

        override fun onReady(event: ReadyEvent) {
            (handlerMap[event::class] as? DiscordEventHandler<ReadyEvent>)?.handle(event)
        }

        override fun onShutdown(event: ShutdownEvent) {
            (handlerMap[event::class] as? DiscordEventHandler<ShutdownEvent>)?.handle(event)
        }

        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            (handlerMap[event::class] as? DiscordEventHandler<SlashCommandInteractionEvent>)?.handle(event)
        }
    }

    @Bean
    fun getDiscordClient(
        discordProperty: DiscordClientProperty,
        eventListener: EventListener
    ): JDA {
        return light(discordProperty.token, enableCoroutines = true).apply {
            addEventListener(eventListener)
        }
    }
}