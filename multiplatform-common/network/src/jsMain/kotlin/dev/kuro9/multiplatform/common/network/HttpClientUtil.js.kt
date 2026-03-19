package dev.kuro9.multiplatform.common.network

import io.ktor.client.*

actual fun httpClient(config: io.ktor.client.HttpClientConfig<*>.() -> Unit) = HttpClient {
    config(this)

    engine {
        // TODO
    }
}