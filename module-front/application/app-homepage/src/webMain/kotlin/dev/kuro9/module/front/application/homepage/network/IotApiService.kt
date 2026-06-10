package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.types.app.homepage.iot.DeviceSwitchRequest
import dev.kuro9.multiplatform.common.types.smartthings.SmartAppUserDevice
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class IotApiService(serverInfo: ServerInfo, tokenRefreshService: TokenRefreshService) {
    private val httpClient = getDefaultHttpClient(serverInfo, tokenRefreshService)

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