package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.serialization.Serializable

@Serializable
data class StringResourcePostRequest(
    val description: String?,
    val string: String,
    val allowed: List<String>,
)

@Serializable
data class StringResourceModifyRequest(
    val description: String?,
    val string: String?,
    val allowed: List<String>?,
)

