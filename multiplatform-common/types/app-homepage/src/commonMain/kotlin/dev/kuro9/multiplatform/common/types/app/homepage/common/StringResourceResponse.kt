package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class StringResourceResponse(
    val description: String?,
    val string: String,
    val allowed: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Serializable
data class StringResourceListResponse(
    val resources: List<Element>
) {
    @Serializable
    data class Element @OptIn(ExperimentalUuidApi::class) constructor(
        val externalId: Uuid,
        val description: String?,
        val string: String,
        val allowed: List<String>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}