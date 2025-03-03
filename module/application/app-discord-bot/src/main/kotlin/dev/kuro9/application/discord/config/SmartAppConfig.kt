package dev.kuro9.application.discord.config

import dev.kuro9.internal.smartapp.api.model.SmartAppProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SmartAppConfig {

    @Bean
    fun smartAppProperty(@Value("\${dev.kuro9.smartapp.token}") token: String): SmartAppProperty =
        object : SmartAppProperty {
            override val token: String = token
        }
}