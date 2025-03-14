package dev.kuro9.application.test

import dev.kuro9.internal.smartapp.api.client.SmartAppApiClient
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppToken
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppDeviceListResponse
import dev.kuro9.multiplatform.common.types.testapp.request.SmartAppSwitchControlRequest
import org.springframework.web.bind.annotation.*

@[RestController RequestMapping("/test")]
class TestEndpointController(private val smartAppApiClient: SmartAppApiClient) {

    @GetMapping
    suspend fun test(): SmartAppDeviceListResponse? {
        val t = smartAppApiClient.listDevices()
        return t
    }

    @PostMapping("/switch")
    suspend fun switchControl(
        @RequestBody body: SmartAppSwitchControlRequest,
    ) {
        smartAppApiClient.executeDeviceCommand(
            deviceId = body.deviceId,
            request = SmartAppDeviceCommandRequest.switch(statusTo = body.value),
            smartAppToken = SmartAppToken.of("")
        )
    }
}