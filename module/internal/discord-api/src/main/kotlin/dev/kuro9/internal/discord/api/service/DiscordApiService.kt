package dev.kuro9.internal.discord.api.service

import dev.kuro9.internal.discord.api.config.DiscordApiConfigProperties
import dev.kuro9.internal.discord.api.dto.DiscordApiRateLimitResponse
import dev.kuro9.internal.discord.api.exception.DiscordApiException
import dev.kuro9.internal.discord.api.resource.DiscordApiResource
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.discord.api.DiscordGuild
import dev.kuro9.multiplatform.common.types.discord.api.DiscordGuildMember
import io.github.harryjhin.slf4j.extension.info
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.springframework.stereotype.Service
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Service
@OptIn(ExperimentalAtomicApi::class)

class DiscordApiService(
    private val properties: DiscordApiConfigProperties
) {
    private val httpClient = httpClient {
        install(Logging)
        install(Resources)
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { cause, request ->
                when (cause) {
                    is ClientRequestException -> when (cause.response.status) {
                        HttpStatusCode.NotFound -> throw DiscordApiException.NotFound("Not Found")
                        HttpStatusCode.TooManyRequests -> throw DiscordApiException.TooManyRequests("Rate limit exceeded")
                    }

                    !is ResponseException -> return@handleResponseExceptionWithRequest
                }

                throw DiscordApiException(
                    code = cause.response.status.value,
                    message = cause.response.status.description,
                    cause = cause,
                )
            }
        }

        defaultRequest {
            url {
                host = "discord.com"
                protocol = URLProtocol.HTTPS
            }
            userAgent("kuro9-backend (kuro9.dev, 0.0.1)")
            headers {
                set("Authorization", "Bot ${properties.token}")
            }

        }

        expectSuccess = true
    }

    private val rateLimitTime = AtomicReference<Instant?>(null)

    @Throws(DiscordApiException::class)
    suspend fun getGuildMemberInfo(guildId: Long, userId: Long): DiscordGuildMember = handleRateLimit {
        httpClient.get(DiscordApiResource.Guild.Member(guildId = guildId, userId = userId))
    }

    /**
     * @param before 페이지네이션용. 주어진 guildId보다 이전인 리스트 가져오기.
     * @param after 페이지네이션용. 주어진 guildId보다 이후인 리스트 가져오기.
     * @param userToken 사용자 oauth2 토큰. null 시 bot 모드로 동작.
     */
    @Throws(DiscordApiException::class)
    suspend fun getMyGuildList(
        before: String? = null,
        after: String? = null,
        userToken: String? = null,
    ): List<DiscordGuild> {
        return when (userToken) {
            null -> {
                // bot mode
                handleRateLimit {
                    httpClient.get(DiscordApiResource.User.Me.Guilds())
                }
            }

            else -> {
                // user mode, no rate limit
                httpClient.get(DiscordApiResource.User.Me.Guilds()) {
                    headers {
                        set("Authorization", "Bearer $userToken")
                    }
                }
            }
        }.body()
    }

    @Throws(DiscordApiException::class)
    private suspend inline fun <reified R> handleRateLimit(call: () -> HttpResponse): R {
        val limitedTime = rateLimitTime.load()
        if (limitedTime != null && limitedTime >= Clock.System.now()) {
            throw DiscordApiException.TooManyRequests("Rate limit exceeded")
        }

        if (limitedTime != null && limitedTime < Clock.System.now()) {
            rateLimitTime.store(null)
        }

        val response = call()
        if (response.status.value == 429) {
            val body = response.body<DiscordApiRateLimitResponse>()
            info { "Rate limit exceeded. Retry after ${body.retryAfter} seconds. message: ${body.message}, isGlobal : ${body.global}" }
            rateLimitTime.store(Clock.System.now() + body.retryAfter.toLong().seconds)

            throw DiscordApiException.TooManyRequests("Rate limit exceeded")
        }

        return response.body()
    }
}