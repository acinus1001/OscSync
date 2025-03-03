package dev.kuro9.application.discord.config

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.smartapp.api.model.SmartAppProperty
import dev.kuro9.internal.smartapp.webhook.handler.SmartAppWebhookHandler
import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SmartAppConfig {

    @Bean
    fun smartAppProperty(@Value("\${dev.kuro9.smartapp.token}") token: String): SmartAppProperty =
        object : SmartAppProperty {
            override val token: String = token
        }

    @Bean
    fun smartAppWebhookHandler(): SmartAppWebhookHandler = object : SmartAppWebhookHandler {
        override fun onVerifyApp(appId: String, verificationUrl: String) {
            infoLog("SmartAppWebhookHandler onVerifyApp $verificationUrl")
        }

        override fun onInitializePhase(
            appId: String,
            pageId: String,
            prevPageId: String?
        ): SmartAppWebhookResponse.ConfigurationData.InitData {
            infoLog("SmartAppWebhookHandler onInitializePhase $pageId")
            return SmartAppWebhookResponse.ConfigurationData.InitData(
                name = "testname",
                description = "testdescription",
                id = "123",
                permission = emptyList(),
                firstPageId = "123"
            )
        }

        override fun onPagePhase(
            appId: String,
            pageId: String,
            prevPageId: String?
        ): SmartAppWebhookResponse.ConfigurationData.PageData {
            infoLog("SmartAppWebhookHandler onPagePhase $pageId")
            return SmartAppWebhookResponse.ConfigurationData.PageData(
                pageId = pageId,
                nextPageId = null,
                previousPageId = prevPageId,
                complete = true,
                name = "testname",
                settings = emptyList(),
            )
        }

    }
}