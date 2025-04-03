package dev.kuro9.application.batch.discord.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "dev.kuro9.discord")
data class DiscordProperties @ConstructorBinding constructor(
    val errorUrl: String
)
