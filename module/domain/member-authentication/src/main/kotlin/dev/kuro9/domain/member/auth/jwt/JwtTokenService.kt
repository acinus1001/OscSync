@file:OptIn(ExperimentalEncodingApi::class)

package dev.kuro9.domain.member.auth.jwt

import dev.kuro9.multiplatform.common.serialization.minifyJson
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class JwtTokenService {

    inline fun <reified T : JwtBasicPayload> makeToken(jwtPayload: T, secretKey: String): String {
        val secretKeyWithSalt = (jwtPayload.sub.toLongOrNull() ?: jwtPayload.sub.hashCode().toLong())
            .shr(jwtPayload.iat.epochSeconds.toInt() % 5)
            .times(jwtPayload.exp.epochSeconds.toInt())
            .plus(jwtPayload.hashCode())
            .let {
                val secretKeyBytes = secretKey.toByteArray(Charsets.UTF_8)
                Random(it).nextBytes(secretKeyBytes.size).zip(secretKeyBytes)
                    .map { (a, b) -> a xor b }
                    .toByteArray()
            }

        val header = """{"alg":"HS512","typ":"JWT"}"""
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val payload = minifyJson.encodeToString(jwtPayload)
            .toByteArray(Charsets.UTF_8)
            .encodeWithNoPadding()

        val signature = Mac.getInstance("HmacSHA512").run {
            init(SecretKeySpec(secretKeyWithSalt, algorithm))
            doFinal("$header.$payload".toByteArray(Charsets.UTF_8))
        }
            .encodeWithNoPadding()

        return "$header.$payload.$signature"
    }

    fun ByteArray.encodeWithNoPadding(): String {
        return Base64.encode(this).dropLastWhile { it == '=' }
    }
}