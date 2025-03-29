package dev.kuro9.internal.discord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dev.kuro9.discord")
data class DiscordConfigProperties(
    val token: String,
    private val testGuild: String?,
) {
    val testGuildLong: Long? = testGuild?.toLongOrNull()
}