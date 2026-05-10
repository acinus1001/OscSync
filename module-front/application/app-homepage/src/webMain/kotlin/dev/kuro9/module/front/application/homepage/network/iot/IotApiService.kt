package dev.kuro9.module.front.application.homepage.network.iot

import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.app.homepage.iot.DeviceSwitchRequest
import dev.kuro9.multiplatform.common.types.smartthings.SmartAppUserDevice
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.setBody
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class IotApiService(private val serverInfo: ServerInfo) {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
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
    }

    suspend fun getRootIotDevices(): List<SmartAppUserDevice> {
        return httpClient.get("/services/iot/root/devices").body<List<SmartAppUserDevice>>()
    }

    suspend fun executeRootDevices(
        deviceId: String,
        desireState: Boolean,
    ) {
        httpClient.put("/services/iot/root/devices") {
            url {
                appendPathSegments(deviceId, "switch")
            }
            setBody(DeviceSwitchRequest(desireState))
        }
    }
}