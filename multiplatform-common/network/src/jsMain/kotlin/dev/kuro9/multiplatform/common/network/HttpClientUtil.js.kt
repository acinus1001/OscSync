package dev.kuro9.multiplatform.common.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials

actual fun httpClient(config: io.ktor.client.HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)

    engine {
        configureRequest {
            credentials = RequestCredentials.INCLUDE
        }
    }
}