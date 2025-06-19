package dev.kuro9.internal.discord

import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.jdabuilder.default
import kotlinx.coroutines.InternalCoroutinesApi
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordClientConfig {

    @OptIn(InternalCoroutinesApi::class)
    @Bean
    fun getDiscordClient(
        discordProperty: DiscordConfigProperties,
        eventHandler: List<DiscordEventHandler<*>>,
    ): JDA {
        return default(discordProperty.token, enableCoroutines = true).apply {
            gatewayIntents += GatewayIntent.GUILD_MESSAGES
            gatewayIntents += GatewayIntent.DIRECT_MESSAGES
            addEventListener(*eventHandler.toTypedArray())
            awaitReady()
            eventHandler.forEach {
                it.initialize(this)
            }
        }
    }
}