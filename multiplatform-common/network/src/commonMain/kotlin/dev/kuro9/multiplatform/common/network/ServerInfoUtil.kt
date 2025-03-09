package dev.kuro9.multiplatform.common.network

import OscSync.multiplatform_common.network.BuildConfig
import io.ktor.http.*

fun getServerInfo() = ServerInfo(
    host = BuildConfig.API_SERVER_HOST,
    port = BuildConfig.API_SERVER_PORT,
    protocol = URLProtocol.createOrDefault(BuildConfig.API_SERVER_METHOD)
)

data class ServerInfo(
    val host: String,
    val port: Int,
    val protocol: URLProtocol,
)