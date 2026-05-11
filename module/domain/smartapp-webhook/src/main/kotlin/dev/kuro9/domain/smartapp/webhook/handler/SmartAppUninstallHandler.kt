package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.UninstallHandler
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import com.smartthings.sdk.smartapp.core.models.UninstallResponseData
import dev.kuro9.domain.smartapp.webhook.repository.SmartAppSubscriptionEntity
import dev.kuro9.domain.smartapp.webhook.repository.SmartAppSubscriptions
import dev.kuro9.domain.smartapp.webhook.service.SmartAppInstallService
import io.github.harryjhin.slf4j.extension.info
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SmartAppUninstallHandler(private val service: SmartAppInstallService) : UninstallHandler {

    @Transactional
    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "UNINSTALL: executionRequest = $executionRequest" }

        val appId = executionRequest.uninstallData.installedApp.installedAppId

        val subs = SmartAppSubscriptionEntity.all()
        for (subInfo in subs) {
            service.deleteSubscription(
                appId = subInfo.appId,
                subscriptionId = subInfo.subscriptionId,
                authToken = subInfo.authToken,
            )
        }
        SmartAppSubscriptions.deleteAll()

        return Response.ok(UninstallResponseData())
    }
}