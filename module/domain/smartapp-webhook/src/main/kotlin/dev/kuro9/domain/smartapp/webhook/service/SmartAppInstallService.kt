package dev.kuro9.domain.smartapp.webhook.service

import com.smartthings.sdk.client.ApiClient
import com.smartthings.sdk.client.methods.SubscriptionsApi
import com.smartthings.sdk.client.models.Subscription
import dev.kuro9.domain.smartapp.webhook.dto.AppSubscriptionRequest
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service

@Service
class SmartAppInstallService(private val apiClient: ApiClient) {


    fun createSubscription(request: AppSubscriptionRequest): Subscription {
        val subscriptionApi = apiClient.buildClient(SubscriptionsApi::class.java)
        info { "request=$request" }
        val result = subscriptionApi.saveSubscription(
            request.appId,
            request.authToken.toBearerString(),
            request.toRequest()
        )
        if (result.id == null) throw IllegalStateException("Subscription Id is null!")
        if (request is AppSubscriptionRequest.CapabilitySubscriptionRequest) return result

        return result
    }

    fun deleteSubscription(appId: String, subscriptionId: String, authToken: String) {
        val subscriptionsApi = apiClient.buildClient(SubscriptionsApi::class.java)
        subscriptionsApi.deleteSubscription(appId, subscriptionId, authToken.toBearerString())
    }

    private fun String.toBearerString() = "Bearer $this"
}