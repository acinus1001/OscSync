package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.serialization.Serializable

@Serializable
data class DiscordIdAndName(
    val id: Long,
    val name: String
)
