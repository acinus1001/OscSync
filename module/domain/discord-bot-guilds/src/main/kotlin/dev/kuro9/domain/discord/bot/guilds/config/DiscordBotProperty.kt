package dev.kuro9.domain.discord.bot.guilds.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("dev.kuro9.discord.bot")
data class DiscordBotProperty @ConstructorBinding constructor(
    val id: Long
)
