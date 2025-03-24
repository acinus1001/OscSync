package dev.kuro9.internal.google.ai.config

import dev.kuro9.internal.google.ai.model.GoogleAiToken
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackageClasses = [GoogleAiConfigProperties::class])
class GoogleAiConfig {

    @Bean
    fun getGeminiToken(
        properties: GoogleAiConfigProperties
    ): GoogleAiToken = GoogleAiToken(properties.apiKey)
}