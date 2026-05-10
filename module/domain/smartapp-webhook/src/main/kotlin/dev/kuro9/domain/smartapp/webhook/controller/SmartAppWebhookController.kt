package dev.kuro9.domain.smartapp.webhook.controller

import com.smartthings.sdk.smartapp.core.SmartApp
import com.smartthings.sdk.smartapp.core.SmartAppDefinition
import com.smartthings.sdk.smartapp.core.extensions.HttpVerificationService
import com.smartthings.sdk.smartapp.core.models.AppLifecycle
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import io.github.harryjhin.slf4j.extension.warn
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.function.Function
import java.util.stream.Collectors

@RestController
@RequestMapping("/webhook/smartapp")
class SmartAppWebhookController(
    smartAppDef: SmartAppDefinition,
    private val httpVerificationService: HttpVerificationService,
) {
    private val smartApp = SmartApp.of(smartAppDef)

    @PostMapping
    fun handle(@RequestBody executionRequest: ExecutionRequest, request: HttpServletRequest): ExecutionResponse {
        val headers: Map<String, String> = request.headerNames.toList().stream()
            .collect(Collectors.toMap(Function.identity()) { request.getHeader(it) })
        if (executionRequest.lifecycle != AppLifecycle.PING
            && !httpVerificationService.verify(request.method, request.requestURI, headers)
        ) {
            warn { "HTTP verification failed" }
            throw IllegalArgumentException("HTTP verification failed")
        }
        return smartApp.execute(executionRequest)
    }
}