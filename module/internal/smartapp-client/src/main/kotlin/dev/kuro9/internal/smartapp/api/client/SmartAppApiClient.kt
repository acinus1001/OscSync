package dev.kuro9.internal.smartapp.api.client

import dev.kuro9.internal.smartapp.api.model.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.model.request.SmartAppToken
import dev.kuro9.internal.smartapp.api.model.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.api.model.response.SmartAppResponse
import org.springframework.http.HttpHeaders
import retrofit2.http.*

interface SmartAppApiClient {

    @GET("/devices")
    suspend fun listDevices(
        @Header(HttpHeaders.AUTHORIZATION) smartAppToken: SmartAppToken,
    ): SmartAppDeviceListResponse

    @POST("/devices/{deviceId}/commands")
    suspend fun executeDeviceCommand(
        @Header(HttpHeaders.AUTHORIZATION) smartAppToken: SmartAppToken,
        @Path("deviceId") deviceId: String,
        @Body request: SmartAppDeviceCommandRequest,
    ): SmartAppResponse.SimpleResult
}