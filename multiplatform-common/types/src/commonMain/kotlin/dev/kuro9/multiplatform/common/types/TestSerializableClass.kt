package dev.kuro9.multiplatform.common.types

import kotlinx.serialization.Serializable

@Serializable
data class TestSerializableClass(
    val name: String,
    val num: Int
)