package dev.kuro9.internal.smartapp.webhook.controller

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.smartapp.webhook.handler.SmartAppWebhookHandler
import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookBody
import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/smartapp/webhook")
@ConditionalOnBean(SmartAppWebhookHandler::class)
class SmartAppWebhookController(private val handler: SmartAppWebhookHandler) {

    @PostMapping
    suspend fun handleWebhook(@RequestBody body: SmartAppWebhookBody): SmartAppWebhookResponse? {
        infoLog("body : \n$body")

        return when (body) {
            is SmartAppWebhookBody.ConfirmationData -> {
                handler.onVerifyApp(
                    appId = body.confirmationData.appId,
                    verificationUrl = body.confirmationData.confirmationUrl,
                )
                null
            }

            is SmartAppWebhookBody.ConfigurationCycle -> when (body.configurationData) {
                is SmartAppWebhookBody.ConfigurationCycle.ConfigurationData.InitPhase ->
                    return handler.onInitializePhase(
                        appId = body.configurationData.installedAppId,
                        pageId = body.configurationData.pageId,
                        prevPageId = body.configurationData.previousPageId,
                    ).let(::SmartAppWebhookResponse)

                is SmartAppWebhookBody.ConfigurationCycle.ConfigurationData.PagePhase ->
                    return handler.onPagePhase(
                        appId = body.configurationData.installedAppId,
                        pageId = body.configurationData.pageId,
                        prevPageId = body.configurationData.previousPageId,
                    ).let(::SmartAppWebhookResponse)
            }
        }
    }
}