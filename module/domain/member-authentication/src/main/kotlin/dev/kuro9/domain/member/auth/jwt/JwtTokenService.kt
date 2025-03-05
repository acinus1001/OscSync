@file:OptIn(ExperimentalEncodingApi::class)

package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.multiplatform.common.serialization.minifyJson
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.jwt.JwtValidationException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class JwtTokenService {

    inline fun <reified T : JwtBasicPayload> makeToken(jwtPayload: T, secretKey: String): JwtToken {
        val header = """{"alg":"HS512","typ":"JWT"}"""
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val payload = minifyJson.encodeToString(jwtPayload)
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val signature = getSignature(
            encodedHeader = header,
            encodedPayload = payload,
            jwtPayload = jwtPayload,
            secretKey = secretKey,
        )

        return JwtToken("$header.$payload.$signature")
    }

    inline fun <reified T : JwtBasicPayload> JwtToken.validateAndGetPayload(secretKey: String): T {
        val (encodedHeader, encodedPayload, signature) = this.token.split('.')

        val payload = Base64.decode(encodedPayload)
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

        return payload
    }

    fun ByteArray.encodeWithNoPadding(): String {
        return Base64.encode(this).dropLastWhile { it == '=' }
    }

    inline fun <reified T : JwtBasicPayload> getSecretKeyWithSalt(jwtPayload: T, secretKey: String): ByteArray {
        return (jwtPayload.sub.toLongOrNull() ?: jwtPayload.sub.hashCode().toLong())
            .shr(jwtPayload.iat.epochSeconds.toInt() % 5)
            .times(jwtPayload.exp.epochSeconds.toInt())
            .plus(jwtPayload.hashCode())
            .let {
                val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
                Random(it).nextBytes(secretKeyBytes.size).zip(secretKeyBytes)
                    .map { (a, b) -> a xor b }
                    .toByteArray()
            }
    }

    inline fun <reified T : JwtBasicPayload> getSignature(
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
}