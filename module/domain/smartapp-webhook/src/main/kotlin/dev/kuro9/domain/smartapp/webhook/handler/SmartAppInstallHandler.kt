package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.client.models.DeviceSubscriptionDetail
import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.InstallHandler
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import com.smartthings.sdk.smartapp.core.models.InstallResponseData
import dev.kuro9.domain.smartapp.webhook.dto.AppSubscriptionRequest
import dev.kuro9.domain.smartapp.webhook.enums.InternalDeviceType
import dev.kuro9.domain.smartapp.webhook.repository.SmartAppSubscriptions
import dev.kuro9.domain.smartapp.webhook.service.SmartAppInstallService
import io.github.harryjhin.slf4j.extension.info
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SmartAppInstallHandler(private val service: SmartAppInstallService) : InstallHandler {

    @Transactional
    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "INSTALL: executionRequest = $executionRequest" }

        val appId = executionRequest.installData.installedApp.installedAppId
        val locationId = executionRequest.installData.installedApp.locationId
        val configMap = executionRequest.installData.installedApp.config
        val authToken = executionRequest.installData.authToken


        InternalDeviceType.entries.forEach { type ->
            configMap[type.internalId]?.let {
                it.forEach { deviceConfig ->
                    with(deviceConfig.deviceConfig) {

                        val response = service.createSubscription(
                            AppSubscriptionRequest.DeviceSubscriptionRequest(
                                appId,
                                authToken,
                                DeviceSubscriptionDetail().apply {
                                    this.deviceId = this@with.deviceId
                                    this.isStateChangeOnly = true
                                    this.componentId = this@with.componentId
                                }
                            )
                        )

                        SmartAppSubscriptions.insert { ins ->
                            ins[this.appId] = appId
                            ins[this.subscriptionId] = response.id
                            ins[this.deviceId] = this@with.deviceId
                            ins[this.authToken] = authToken
                        }
                    }
                }
            }

        }

        return Response.ok(InstallResponseData())
    }
}