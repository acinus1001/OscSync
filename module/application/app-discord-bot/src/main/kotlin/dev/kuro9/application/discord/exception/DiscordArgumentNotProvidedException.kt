package dev.kuro9.application.discord.exception

class DiscordArgumentNotProvidedException(
    val argumentNames: List<String>,
) : IllegalArgumentException("discord argument not provided: $argumentNames")