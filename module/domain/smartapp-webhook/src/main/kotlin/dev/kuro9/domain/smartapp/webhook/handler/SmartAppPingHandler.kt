package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.smartapp.core.internal.handlers.DefaultPingHandler
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service

@Service
class SmartAppPingHandler : DefaultPingHandler() {
    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "PING: executionRequest = $executionRequest" }
        return super.handle(executionRequest)
    }
}