package dev.kuro9.internal.google.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "dev.kuro9.gemini")
data class GoogleAiConfigProperties @ConstructorBinding constructor(
    val apiKey: String,
)