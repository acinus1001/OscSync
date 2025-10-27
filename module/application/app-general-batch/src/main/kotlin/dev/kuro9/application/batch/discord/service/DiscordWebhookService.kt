package dev.kuro9.application.batch.discord.service

import dev.kuro9.application.batch.discord.dto.DiscordWebhookPayload
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.github.harryjhin.slf4j.extension.error
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service

@Service
class DiscordWebhookService {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(Logging)
        expectSuccess = true

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun sendWebhook(url: String, payload: DiscordWebhookPayload) {
        httpClient.post(urlString = url) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun sendWebhookWithRetry(url: String, payload: DiscordWebhookPayload) {
        var exception: Exception? = null

        repeat(3) { i ->
            try {
                return sendWebhook(url, payload)
            } catch (e: Exception) {
                error(e) { "Failed to send webhook. attempt: $i, error: ${e.message}" }
                exception = e
                delay((1 shl i) * 80_000L)
            }
        }

        throw exception ?: IllegalStateException("Failed to send webhook after 3 attempts")
    }
}