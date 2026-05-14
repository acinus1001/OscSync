package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*

class AuthResourceApiService(serverInfo: ServerInfo) {
    private val httpClient = getDefaultHttpClient(serverInfo)

    suspend fun getStringResource(id: String): String {
        return httpClient.get("/resources/strings") {
            url {
                appendPathSegments(id)
            }
        }.bodyAsText(Charsets.UTF_8)
    }

    suspend inline fun <reified T> getJsonResources(id: String): T {
        val text = getStringResource(id)
        return minifyJson.decodeFromString(text)
    }

    suspend fun getImageResource(id: String): ByteArray {
        return httpClient.get("/resources/images") {
            url {
                appendPathSegments(id)
            }
        }.bodyAsBytes()
    }
}