package dev.kuro9.module.front.application.homepage.network.common

import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.network.httpClient
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class TokenRefreshService(
    private val serverInfo: ServerInfo,
) {
    private val refreshTokenClient = httpClient {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(Logging)
        defaultRequest {
            url {
                host = serverInfo.host
                port = serverInfo.port
                protocol = serverInfo.protocol
                path("auth", "refresh")
            }
        }
    }
    private var refreshJob: Deferred<Boolean>? = null

    suspend fun tryRefresh(): Boolean {
        refreshJob?.let {
            return it.await()
        }

        val job = CompletableDeferred<Boolean>()
        refreshJob = job

        try {
            val response = refreshTokenClient.post { }
            val success = response.status.isSuccess()

            job.complete(success)
            return success
        } catch (e: Exception) {
            e.printStackTrace()
            job.complete(false)
            return false
        } finally {
            refreshJob = null
        }
    }
}