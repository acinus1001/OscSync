package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.multiplatform.common.serialization.serializer.instant.UnixTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class JwtRefreshPayload(
    override val sub: String,
    override val name: String,
    override val iat: UnixTimestamp,
    override val exp: UnixTimestamp,
) : JwtBasicPayload {
    override val scp: List<String> = emptyList()
    val type: String = "REFRESH"
}