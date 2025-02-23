package dev.kuro9.internal.smartapp.client

import dev.kuro9.internal.smartapp.model.response.SmartAppDeviceListResponse
import retrofit2.http.GET

interface SmartAppApiClient {

    @GET("/devices")
    suspend fun listDevices(): SmartAppDeviceListResponse
}