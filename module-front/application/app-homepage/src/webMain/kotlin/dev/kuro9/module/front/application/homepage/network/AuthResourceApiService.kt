package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*

class AuthResourceApiService(
    serverInfo: ServerInfo,
    tokenRefreshService: TokenRefreshService,
    userViewModel: UserViewModel
) {
    private val httpClient = getDefaultHttpClient(serverInfo, tokenRefreshService, userViewModel)

    suspend fun getStringResource(id: String, nullOn401Or403: Boolean = true): String? {
        val data = runCatching {
            httpClient.get("/resources/strings") {
                url {
                    appendPathSegments(id)
                }
            }.bodyAsText(Charsets.UTF_8)
        }

        return when (nullOn401Or403) {
            true -> data.recover { e ->
                if (e !is ClientRequestException) throw e
                if (e.response.status !in listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden)) throw e

                null
            }.getOrThrow()

            false -> data.getOrThrow()
        }
    }

    suspend inline fun <reified T> getJsonResources(id: String, nullOn401Or403: Boolean = true): T? {
        return getStringResource(id, nullOn401Or403)?.let { minifyJson.decodeFromString<T>(it) }
    }

    suspend fun getImageResource(id: String, nullOn401Or403: Boolean = true): ByteArray? {
        val data = runCatching {
            httpClient.get("/resources/images") {
                url {
                    appendPathSegments(id)
                }
            }.bodyAsBytes()
        }

        return when (nullOn401Or403) {
            true -> data.recover { e ->
                if (e !is ClientRequestException) throw e
                if (e.response.status !in listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden)) throw e

                null
            }.getOrThrow()

            false -> data.getOrThrow()
        }
    }
}