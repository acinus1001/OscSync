package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.ConfigurationHandler
import com.smartthings.sdk.smartapp.core.models.ConfigurationPhase
import com.smartthings.sdk.smartapp.core.models.ConfigurationResponseData
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import io.github.harryjhin.slf4j.extension.info
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class SmartAppConfigurationHandler(
    @param:Qualifier("initializeResponse") private val initalizeResponse: ConfigurationResponseData,
    @param:Qualifier("pageResponse") private val pageResponse: ConfigurationResponseData,
) : ConfigurationHandler {
    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "CONFIGURATION: executionRequest = $executionRequest" }

        return when (executionRequest.configurationData.phase) {
            ConfigurationPhase.INITIALIZE -> Response.ok(initalizeResponse)
            ConfigurationPhase.PAGE -> Response.ok(pageResponse)
            null -> Response.status(400)
        }
    }
}