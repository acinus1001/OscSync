package dev.kuro9.internal.smartapp.api.service

import dev.kuro9.internal.smartapp.api.client.SmartAppApiClient
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppDeviceCommandRequest
import dev.kuro9.internal.smartapp.api.dto.request.SmartAppToken
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppDeviceListResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponse
import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponseObject
import dev.kuro9.internal.smartapp.api.exception.ApiNotSuccessException
import org.springframework.stereotype.Service

@Service
class SmartAppApiService(private val apiClient: SmartAppApiClient) {

    suspend fun listDevices(smartAppToken: SmartAppToken): SmartAppDeviceListResponse =
        withConvertedException { apiClient.listDevices(smartAppToken) }

    suspend fun getDeviceInfo(
        smartAppToken: SmartAppToken,
        deviceId: String
    ): SmartAppResponseObject.DeviceInfo = withConvertedException { apiClient.getDeviceInfo(smartAppToken, deviceId) }

    suspend fun executeDeviceCommand(
        smartAppToken: SmartAppToken,
        deviceId: String,
        request: SmartAppDeviceCommandRequest,
    ): SmartAppResponse.SimpleResult =
        withConvertedException { apiClient.executeDeviceCommand(smartAppToken, deviceId, request) }

    private inline fun <reified T> withConvertedException(provider: () -> T): T {
        return runCatching { provider() }
            .getOrElse {
                if (it is retrofit2.HttpException) {
                    throw ApiNotSuccessException(
                        code = it.code(),
                        httpMessage = it.message(),
                        cause = it,
                    )
                }
                throw it
            }
    }

}