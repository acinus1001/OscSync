package dev.kuro9.application.batch.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookPayload(
    val username: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val content: String? = null,
    val embeds: List<DiscordEmbed> = emptyList(),
    val attachments: List<String> = emptyList(),
)
