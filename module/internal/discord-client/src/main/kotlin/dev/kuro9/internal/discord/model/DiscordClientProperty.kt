package dev.kuro9.internal.discord.model

// Must be registered as spring bean on use
interface DiscordClientProperty {
    val token: String
}