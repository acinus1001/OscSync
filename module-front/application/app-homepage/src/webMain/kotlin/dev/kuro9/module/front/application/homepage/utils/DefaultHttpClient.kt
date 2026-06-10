package dev.kuro9.module.front.application.homepage.utils

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

fun getDefaultHttpClient(
    serverInfo: ServerInfo,
    tokenRefreshService: TokenRefreshService,
    userViewModel: UserViewModel? = null,
    config: HttpClientConfig<*>.() -> Unit = {}
): HttpClient {

    return httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        defaultRequest {
            url {
                host = serverInfo.host
                port = serverInfo.port
                protocol = serverInfo.protocol
            }
            contentType(ContentType.Application.Json)
        }
        expectSuccess = true

        config()
    }.apply {
        this.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)

            if (
                originalCall.response.status != HttpStatusCode.Unauthorized ||
                request.url.encodedPath.contains("/auth/refresh")
            ) {
                return@intercept originalCall
            }

            val refreshed = tokenRefreshService.tryRefresh()

            if (!refreshed) {
                println("토큰 리프레시 실패.. 로그아웃 합니다.")
                userViewModel?.onRefreshFailure()
                return@intercept originalCall
            }

            println("토큰 리프레시 성공. 재시도합니다.")
            userViewModel?.onRefreshSuccess()

            execute(request)
        }
    }
}