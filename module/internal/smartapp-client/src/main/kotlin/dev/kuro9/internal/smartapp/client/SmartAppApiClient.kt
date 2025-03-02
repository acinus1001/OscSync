package dev.kuro9.internal.smartapp.client

import dev.kuro9.internal.smartapp.model.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.model.request.SmartAppToken
import dev.kuro9.internal.smartapp.model.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.model.response.SmartAppResponse
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