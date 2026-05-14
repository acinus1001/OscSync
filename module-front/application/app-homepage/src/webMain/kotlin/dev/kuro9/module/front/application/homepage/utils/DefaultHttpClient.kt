package dev.kuro9.module.front.application.homepage.utils

import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

fun getDefaultHttpClient(serverInfo: ServerInfo, config: HttpClientConfig<*>.() -> Unit = {}): HttpClient {
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
    }
}