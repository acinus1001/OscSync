package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CommonApiService(
    serverInfo: ServerInfo,
    tokenRefreshService: TokenRefreshService,
    userViewModel: UserViewModel
) {
    private val httpClient = getDefaultHttpClient(serverInfo, tokenRefreshService, userViewModel)

    suspend fun getBulkGuildInfo(guildIdList: List<Long>): List<DiscordGuildInfo> {
        return httpClient.post("/common/info/guilds/bulk") {
            contentType(ContentType.Application.Json)
            setBody(guildIdList)
        }.body<List<DiscordGuildInfo>>()
    }
}
