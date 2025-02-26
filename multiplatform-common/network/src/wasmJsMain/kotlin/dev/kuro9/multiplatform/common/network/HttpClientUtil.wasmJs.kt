package dev.kuro9.multiplatform.common.network

import io.ktor.client.*

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient {
    config(this)

    engine {
        // TODO
    }
}