package dev.kuro9.internal.discord.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("dev.kuro9.discord.api")
data class DiscordApiConfigProperties @ConstructorBinding constructor(
    val token: String,
)
