package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordIdAndName
import io.ktor.client.call.*
import io.ktor.client.request.*

class DiscordNameApiService(serverInfo: ServerInfo) {
    private val httpClient = getDefaultHttpClient(serverInfo)

    suspend fun searchNames(keyword: String): List<DiscordIdAndName> {
        return httpClient.get("/names/search") {
            parameter("keyword", keyword)
        }.body<List<DiscordIdAndName>>()
    }
}
