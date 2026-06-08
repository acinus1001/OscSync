package dev.kuro9.domain.member.auth.network

import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DiscordOAuthApiService(
    @param:Value($$"${spring.security.oauth2.client.registration.discord.client-id}")
    private val clientId: String,
    @param:Value($$"${spring.security.oauth2.client.registration.discord.client-secret}")
    private val clientSecret: String
) {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(Resources)

        defaultRequest {
            url {
                host = "discord.com"
                protocol = URLProtocol.HTTPS
            }
            contentType(ContentType.Application.FormUrlEncoded)
        }
        expectSuccess = true
    }

    suspend fun refreshToken(refreshToken: String): DiscordOAuthRefreshResponse {
        return httpClient.submitForm(
            url = "https://discord.com/api/oauth2/token",
            formParameters = parameters {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("grant_type", "refresh_token")
                append("refresh_token", refreshToken)
            }
        ).body<DiscordOAuthRefreshResponse>()
    }

    suspend fun revokeToken(vararg token: String) {
        for (t in token) {
            httpClient.submitForm(
                url = "https://discord.com/api/oauth2/token/revoke",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("token", t)
                }
            )
        }
    }
}