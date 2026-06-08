package dev.kuro9.domain.member.auth.network

import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.date.util.plus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
data class DiscordOAuthRefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long, // seconds
    @SerialName("refresh_token") val refreshToken: String,
) {
    val expiresAt: LocalDateTime = LocalDateTime.now() + expiresIn.seconds
}