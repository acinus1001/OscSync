package dev.kuro9.internal.smartapp.webhook.controller

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookBody
import dev.kuro9.multiplatform.common.serialization.minifyJson
import org.intellij.lang.annotations.Language
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/smartapp/webhook")
class SmartAppWebhookController {

    @PostMapping
    suspend fun handleWebhook(body: SmartAppWebhookBody) {
        infoLog("body : \n$body")
    }
}

fun main() {
    @Language("JSON") val testStr = """
        {"lifecycle":"CONFIRMATION","executionId":"27D4C5A79112454A9FAA0811DD058675-P3XS","appId":"9f5ebb90-7c81-44cc-99c5-1193c0916c4a","locale":"en","version":"0.1.0","confirmationData":{"appId":"9f5ebb90-7c81-44cc-99c5-1193c0916c4a","confirmationUrl":"https://api.smartthings.com/apps/9f5ebb90-7c81-44cc-99c5-1193c0916c4a/confirm-registration?token=e13c4327-134e-4fa6-a4eb-18eafd1cad32&nonce=726f905a-4699-48ee-95a7-e588079672c6"},"settings":{}}
    """.trimIndent()

    val data = minifyJson.decodeFromString<SmartAppWebhookBody>(testStr)

    println(data)
}