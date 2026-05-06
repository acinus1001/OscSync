package dev.kuro9.internal.discord.api.exception

open class DiscordApiException(
    val code: Int,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)