package dev.kuro9.application.discord.config

import dev.kuro9.internal.discord.model.DiscordClientProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application-discord.yml")
class DiscordConfig {

    @Bean
    fun getDiscordBotProperty(
        @Value("\${dev.kuro9.discord.token}") token: String,
    ): DiscordClientProperty = object : DiscordClientProperty {
        override val token: String = token
    }
}