@file:OptIn(ExperimentalTime::class)

package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.multiplatform.common.serialization.serializer.instant.UnixTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
class JwtPayloadV1(
    override val sub: String,
    override val name: String,
    override val iat: UnixTimestamp,
    override val exp: UnixTimestamp,
    override val scp: List<String>,
    @SerialName("avatar_url") val avatarUrl: String?,
) : JwtBasicPayload