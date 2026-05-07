package dev.kuro9.internal.discord.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordApiRateLimitResponse(
    val message: String,
    @SerialName("retry_after") val retryAfter: Double,
    val global: Boolean,
)
