package dev.kuro9.multiplatform.common.network.discord.app

import dev.kuro9.multiplatform.common.network.getServerInfo
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*

fun typeSafeHttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient = httpClient {
    config(this)
    install(ContentNegotiation) { json(minifyJson) }
    install(Resources)
    install(HttpCookies)
    install(Logging)
    defaultRequest {
        val (serverHost, serverPort, serverProtocol) = getServerInfo()
        host = serverHost
        port = serverPort
        url { protocol = serverProtocol }
    }
}

