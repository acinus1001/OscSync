package dev.kuro9.internal.discord.api.exception

open class DiscordApiException(
    val code: Int,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {

    class NotFound(override val message: String) : DiscordApiException(404, message)
    class TooManyRequests(override val message: String) : DiscordApiException(429, message)
}