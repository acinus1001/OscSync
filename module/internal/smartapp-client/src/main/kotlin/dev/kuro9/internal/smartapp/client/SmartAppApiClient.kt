package dev.kuro9.internal.smartapp.client

import dev.kuro9.internal.smartapp.model.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.model.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.model.response.SmartAppResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SmartAppApiClient {

    @GET("/devices")
    suspend fun listDevices(): SmartAppDeviceListResponse

    @POST("/devices/{deviceId}/commands")
    suspend fun executeDeviceCommand(
        @Path("deviceId") deviceId: String,
        @Body request: SmartAppDeviceCommandRequest,
    ): SmartAppResponse.SimpleResult
}