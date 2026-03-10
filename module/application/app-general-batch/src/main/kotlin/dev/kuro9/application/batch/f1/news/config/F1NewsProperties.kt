package dev.kuro9.application.batch.f1.news.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "dev.kuro9.f1")
data class F1NewsProperties @ConstructorBinding constructor(
    val geminiApiKey: String,
)
