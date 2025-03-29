package dev.kuro9.application.discord.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ErrorWebhookConfig {

    @Bean
    fun errorWebhookUrl(
        @Value("\${dev.kuro9.webhook.url}") url: String
    ): ErrorWebhookUrl {
        return ErrorWebhookUrl(url)
    }

    data class ErrorWebhookUrl(val url: String)
}