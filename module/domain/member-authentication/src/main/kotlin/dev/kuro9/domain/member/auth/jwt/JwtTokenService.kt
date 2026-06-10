@file:OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)

package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.domain.member.auth.config.JwtTokenConfigProperties
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.member.auth.repository.MemberAuthorities
import dev.kuro9.domain.member.auth.repository.MemberEntity
import dev.kuro9.domain.member.auth.service.RefreshTokenInfo
import dev.kuro9.domain.member.auth.service.RefreshTokenService
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.date.util.toLocalDateTime
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.github.harryjhin.slf4j.extension.info
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@Service
class JwtTokenService(
    private val properties: JwtTokenConfigProperties,
    private val refreshTokenService: RefreshTokenService,
) {
    private val noPaddingBase64 = Base64.withPadding(Base64.PaddingOption.ABSENT)
    private val accessTokenExpireDuration = 30.minutes
    private val refreshTokenExpireDuration = 7.days

    fun getUserId(authentication: Authentication): Long {
        val userDetail = authentication.principal as DiscordUserDetail
        return userDetail.id
    }

    fun makeTokenResponse(authentication: Authentication): JwtTokenResponse {
        val userDetail = authentication.principal as DiscordUserDetail
        val now = Clock.System.now()

        val accessPayload = JwtPayloadV1(
            sub = userDetail.id.toString(),
            name = userDetail.userName,
            iat = now,
            exp = now + accessTokenExpireDuration,
            scp = MemberAuthorities.select(MemberAuthorities.authority)
                .where { MemberAuthorities.member eq userDetail.id }
                .map { it[MemberAuthorities.authority] }
                .plus(authentication.authorities.map { it.authority })
                .distinct(),
            avatarUrl = userDetail.avatarUrl,
        )

        val refreshPayload = JwtRefreshPayload(
            sub = userDetail.id.toString(),
            name = userDetail.userName,
            iat = now,
            exp = now + refreshTokenExpireDuration,
        )

        val accessToken = makeToken(accessPayload, properties.key)
        val refreshToken = makeToken(refreshPayload, properties.key)

        refreshTokenService.saveToken(
            userId = userDetail.id,
            token = refreshToken.token,
            expiresAt = refreshPayload.exp.toLocalDateTime(),
            createdAt = now.toLocalDateTime()
        )

        return JwtTokenResponse(
            accessToken = accessToken.token,
            refreshToken = refreshToken.token
        )
    }

    fun isRefreshable(
        refreshTokenStr: String,
        savedToken: RefreshTokenInfo?
    ): Boolean = run {
        val payload = validateAndGetRefreshPayload(JwtToken(refreshTokenStr))

        if (payload.type != "REFRESH") return@run false

        savedToken
            ?.takeIf { it.expiresAt > LocalDateTime.now() }
            ?: return@run false

        return@run true
    }.also { info { "isRefreshable : $it" } }

    @Transactional
    fun refreshToken(
        refreshTokenStr: String,
        savedToken: RefreshTokenInfo?
    ): JwtTokenResponse {
        val payload = validateAndGetRefreshPayload(JwtToken(refreshTokenStr))

        if (payload.type != "REFRESH") {
            throw JwtValidationException(
                "Not a refresh token", listOf(
                    OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "Not a refresh token.",
                        null
                    )
                )
            )
        }

        savedToken ?: throw JwtValidationException(
            "Refresh token not found or already used", listOf(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "Refresh token not found or already used.",
                    null
                )
            )
        )

        // Rotation: Delete old token
        refreshTokenService.deleteByToken(refreshTokenStr)

        if (savedToken.expiresAt <= LocalDateTime.now())
            throw JwtValidationException(
                "Refresh token has expired", listOf(
                    OAuth2Error(
                        OAuth2ErrorCodes.INVALID_TOKEN,
                        "Refresh token has expired.",
                        null
                    )
                )
            )

        val memberInfo = MemberEntity.findById(savedToken.userId) ?: throw IllegalStateException("Member not found")
        val grantAuthority = listOf(memberInfo.role.name) + memberInfo.authorities.map { it.authority }

        val now = Clock.System.now()
        val accessPayload = JwtPayloadV1(
            iat = now,
            exp = now + accessTokenExpireDuration,
            scp = grantAuthority,
            sub = memberInfo.id.value.toString(),
            name = memberInfo.name,
            avatarUrl = memberInfo.avatarUrl,
        )

        val newRefreshPayload = payload.copy(
            iat = now,
            exp = now + refreshTokenExpireDuration,
        )

        val newAccessToken = makeToken(accessPayload, properties.key)
        val newRefreshToken = makeToken(newRefreshPayload, properties.key)

        refreshTokenService.saveToken(
            userId = payload.sub.toLong(),
            token = newRefreshToken.token,
            expiresAt = newRefreshPayload.exp.toLocalDateTime(),
            createdAt = now.toLocalDateTime()
        )

        return JwtTokenResponse(
            accessToken = newAccessToken.token,
            refreshToken = newRefreshToken.token
        )
    }

    fun getUserIdWithNoCheck(accessToken: JwtToken): Long {
        return accessToken.getPayload<JwtPayloadV1>().sub.toLong()
    }

    @Throws(JwtValidationException::class, SerializationException::class)
    fun validateAndGetPayload(token: JwtToken): JwtPayloadV1 {
        return token.validateAndGetPayload(properties.key)
    }

    @Throws(JwtValidationException::class, SerializationException::class)
    fun validateAndGetRefreshPayload(token: JwtToken): JwtRefreshPayload {
        return token.validateAndGetPayload(properties.key)
    }

    fun isValid(token: JwtToken): Boolean = token.isValid<JwtPayloadV1>(properties.key)


    private inline fun <reified T : JwtBasicPayload> makeToken(jwtPayload: T, secretKey: String): JwtToken {
        val encodedHeader = """{"alg":"HS512","typ":"JWT"}"""
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val payload = minifyJson.encodeToString(jwtPayload)
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val signature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = payload,
            jwtPayload = jwtPayload,
            secretKey = secretKey,
        )

        return JwtToken("$encodedHeader.$payload.$signature")
    }

    private inline fun <reified T : JwtBasicPayload> JwtToken.getPayload(): T {
        val (_, encodedPayload, _) = this.token.split('.')

        val payload = noPaddingBase64.decode(encodedPayload)
            .toString(Charsets.UTF_8)
            .let<String, T>(minifyJson::decodeFromString)

        return payload
    }

    @Throws(JwtValidationException::class, SerializationException::class)
    private inline fun <reified T : JwtBasicPayload> JwtToken.validateAndGetPayload(secretKey: String): T {
        val (encodedHeader, encodedPayload, signature) = this.token.split('.')

        val payload = noPaddingBase64.decode(encodedPayload)
            .toString(Charsets.UTF_8)
            .let<String, T>(minifyJson::decodeFromString)

        val expectSignature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = encodedPayload,
            jwtPayload = payload,
            secretKey = secretKey,
        )

        if (expectSignature != signature) throw JwtValidationException(
            "jwt is malformed.", listOf(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "jwt is not valid.",
                    null
                )
            )
        )

        if (Clock.System.now() >= payload.exp) throw JwtValidationException(
            "jwt has expired.", listOf(
                OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "jwt has expired.",
                    null
                )
            )
        )

        return payload
    }

    private inline fun <reified T : JwtBasicPayload> JwtToken.isValid(secretKey: String): Boolean {
        val (encodedHeader, encodedPayload, signature) = this.token.split('.')

        val payload = noPaddingBase64.decode(encodedPayload)
            .toString(Charsets.UTF_8)
            .let<String, T>(minifyJson::decodeFromString)

        val expectSignature = getSignature(
            encodedHeader = encodedHeader,
            encodedPayload = encodedPayload,
            jwtPayload = payload,
            secretKey = secretKey,
        )

        return expectSignature == signature && (Clock.System.now() < payload.exp)
    }

    private inline fun <reified T : JwtBasicPayload> getSecretKeyWithSalt(jwtPayload: T, secretKey: String): ByteArray {
        return (jwtPayload.sub.toLong())
            .shr(jwtPayload.iat.epochSeconds.toInt() % 5)
            .times(jwtPayload.exp.epochSeconds.toInt())
            .let {
                val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
                Random(it).nextBytes(secretKeyBytes.size).zip(secretKeyBytes)
                    .map { (a, b) -> a xor b }
                    .toByteArray()
            }
    }

    private inline fun <reified T : JwtBasicPayload> getSignature(
        encodedHeader: String,
        encodedPayload: String,
        jwtPayload: T,
        secretKey: String
    ): String {
        return Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(getSecretKeyWithSalt(jwtPayload, secretKey), algorithm))
            doFinal("$encodedHeader.$encodedPayload".toByteArray(Charsets.UTF_8))
        }
            .encodeWithNoPadding()
    }

    fun ByteArray.encodeWithNoPadding(): String {
        return noPaddingBase64.encode(this)
    }
}