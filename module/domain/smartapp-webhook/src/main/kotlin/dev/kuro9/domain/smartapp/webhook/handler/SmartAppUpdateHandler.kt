package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.client.models.DeviceSubscriptionDetail
import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.UpdateHandler
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import com.smartthings.sdk.smartapp.core.models.UpdateResponseData
import dev.kuro9.domain.smartapp.webhook.dto.AppSubscriptionRequest
import dev.kuro9.domain.smartapp.webhook.enums.InternalDeviceType
import dev.kuro9.domain.smartapp.webhook.repository.SmartAppSubscriptionEntity
import dev.kuro9.domain.smartapp.webhook.repository.SmartAppSubscriptions
import dev.kuro9.domain.smartapp.webhook.service.SmartAppInstallService
import io.github.harryjhin.slf4j.extension.info
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SmartAppUpdateHandler(
    private val service: SmartAppInstallService,
) : UpdateHandler {

    @Transactional
    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "UPDATE: executionRequest = $executionRequest" }

        info { "Deleting Exist Devices..." }
        val subs = SmartAppSubscriptionEntity.all()
        for (subInfo in subs) {
            service.deleteSubscription(
                appId = subInfo.appId,
                subscriptionId = subInfo.subscriptionId,
                authToken = subInfo.authToken,
            )
        }
        SmartAppSubscriptions.deleteAll()

        val appId = executionRequest.updateData.installedApp.installedAppId
        val locationId = executionRequest.updateData.installedApp.locationId
        val configMap = executionRequest.updateData.installedApp.config
        val authToken = executionRequest.updateData.authToken

        info { "Saving New Devices..." }
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


        return Response.ok(UpdateResponseData())
    }
}