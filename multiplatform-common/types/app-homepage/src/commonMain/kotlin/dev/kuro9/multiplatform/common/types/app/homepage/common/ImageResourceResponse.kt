@file:OptIn(ExperimentalUuidApi::class)

package dev.kuro9.multiplatform.common.types.app.homepage.common

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ImageResourceResponse(
    val externalId: Uuid,
    val description: String?,
    val allowed: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)


@Serializable
data class ImageResourceListResponse(
    val resources: List<Element>
) {
    @Serializable
    data class Element(
        val externalId: Uuid,
        val description: String?,
        val allowed: List<String>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}