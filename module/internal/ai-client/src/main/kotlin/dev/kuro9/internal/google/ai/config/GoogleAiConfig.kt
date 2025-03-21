package dev.kuro9.internal.google.ai.config

import dev.kuro9.internal.google.ai.model.GoogleAiToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleAiConfig {

    @Bean
    fun getGeminiToken(
        @Value("\${dev.kuro9.gemini.api-key}") token: String,
    ): GoogleAiToken = GoogleAiToken(token)
}