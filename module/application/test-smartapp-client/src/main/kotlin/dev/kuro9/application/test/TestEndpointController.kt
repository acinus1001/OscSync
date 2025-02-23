package dev.kuro9.application.test

import dev.kuro9.internal.smartapp.client.SmartAppApiClient
import dev.kuro9.internal.smartapp.model.response.SmartAppDeviceListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@[RestController RequestMapping("/test")]
class TestEndpointController(private val smartAppApiClient: SmartAppApiClient) {

    @GetMapping
    suspend fun test(): SmartAppDeviceListResponse? {
        val t = smartAppApiClient.listDevices()
        return t
    }
}