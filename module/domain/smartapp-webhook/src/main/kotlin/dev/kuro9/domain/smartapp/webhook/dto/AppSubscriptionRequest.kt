package dev.kuro9.domain.smartapp.webhook.dto

import com.smartthings.sdk.client.models.CapabilitySubscriptionDetail
import com.smartthings.sdk.client.models.DeviceSubscriptionDetail
import com.smartthings.sdk.client.models.SubscriptionRequest
import com.smartthings.sdk.client.models.SubscriptionSource

sealed interface AppSubscriptionRequest {
    val appId: String
    val authToken: String
    val sourceType: SubscriptionSource

    data class DeviceSubscriptionRequest(
        override val appId: String,
        override val authToken: String,
        val device: DeviceSubscriptionDetail
    ) : AppSubscriptionRequest {
        override val sourceType = SubscriptionSource.DEVICE
    }

    data class CapabilitySubscriptionRequest(
        override val appId: String,
        override val authToken: String,
        val capability: CapabilitySubscriptionDetail
    ) : AppSubscriptionRequest {
        override val sourceType = SubscriptionSource.CAPABILITY
    }

    fun toRequest(): SubscriptionRequest {
        val request = SubscriptionRequest().apply {
            sourceType = this@AppSubscriptionRequest.sourceType
        }
        when (this) {
            is DeviceSubscriptionRequest -> {
                request.device = device
            }

            is CapabilitySubscriptionRequest -> {
                request.capability = capability
            }
        }

        return request
    }
}