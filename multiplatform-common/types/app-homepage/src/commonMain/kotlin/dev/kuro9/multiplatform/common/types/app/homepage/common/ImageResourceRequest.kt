package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.serialization.Serializable

@Serializable
data class ImageResourcePostRequest(
    val description: String?,
    val allowed: List<String>,
)

@Serializable
data class ImageResourceModifyRequest(
    val description: String?,
    val allowed: List<String>?,
)