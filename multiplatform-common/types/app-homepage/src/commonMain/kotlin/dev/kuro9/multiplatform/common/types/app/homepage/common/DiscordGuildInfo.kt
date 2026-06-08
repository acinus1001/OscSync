package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.serialization.Serializable

@Serializable
data class DiscordGuildInfo(
    val id: Long,
    val name: String,
    val iconUrl: String?,
)
