package dev.kuro9.domain.member.auth.jwt

import kotlinx.serialization.Serializable

@Serializable
data class JwtTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

@JvmInline
value class JwtToken(val token: String)