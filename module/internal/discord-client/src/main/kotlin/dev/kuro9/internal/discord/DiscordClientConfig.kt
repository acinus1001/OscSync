package dev.kuro9.internal.discord

import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.jdabuilder.default
import kotlinx.coroutines.InternalCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
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
        buttonEventHandler: DiscordEventHandler<ButtonInteractionEvent>?,
        messageEventHandler: DiscordEventHandler<MessageReceivedEvent>?
    ): CoroutineEventListener = CoroutineEventListener { event: GenericEvent ->
        when (event) {
            is ReadyEvent -> readyHandler?.handle(event)
            is ShutdownEvent -> shutdownHandler?.handle(event)
            is SlashCommandInteractionEvent -> slashCommandHandler?.handle(event)
            is CommandAutoCompleteInteractionEvent -> autoCompleteHandler?.handle(event)
            is ButtonInteractionEvent -> buttonEventHandler?.handle(event)
            is MessageReceivedEvent -> messageEventHandler?.handle(event)
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    @Bean
    fun getDiscordClient(
        discordProperty: DiscordConfigProperties,
        eventListener: CoroutineEventListener,
        eventHandler: List<DiscordEventHandler<*>>,
    ): JDA {
        return default(discordProperty.token, enableCoroutines = true).apply {
            gatewayIntents += GatewayIntent.GUILD_MESSAGES
            gatewayIntents += GatewayIntent.DIRECT_MESSAGES
            addEventListener(eventListener)
            awaitReady()
            eventHandler.forEach {
                it.initialize(this)
            }
        }
    }
}