package dev.kuro9.internal.smartapp.api.client

import dev.kuro9.internal.smartapp.api.dto.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppToken
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponseObject.DeviceInfo
import org.springframework.http.HttpHeaders
import retrofit2.http.*

interface SmartAppApiClient {

    @GET("/devices")
    suspend fun listDevices(
        @Header(HttpHeaders.AUTHORIZATION) smartAppToken: SmartAppToken,
    ): SmartAppDeviceListResponse

    @GET("/devices/{deviceId}")
    suspend fun getDeviceInfo(
        @Header(HttpHeaders.AUTHORIZATION) smartAppToken: SmartAppToken,
        @Path("deviceId") deviceId: String
    ): DeviceInfo

    @POST("/devices/{deviceId}/commands")
    suspend fun executeDeviceCommand(
        @Header(HttpHeaders.AUTHORIZATION) smartAppToken: SmartAppToken,
        @Path("deviceId") deviceId: String,
        @Body request: SmartAppDeviceCommandRequest,
    ): SmartAppResponse.SimpleResult
}