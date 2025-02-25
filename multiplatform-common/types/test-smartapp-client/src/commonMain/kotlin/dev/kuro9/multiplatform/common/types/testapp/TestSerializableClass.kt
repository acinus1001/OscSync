package dev.kuro9.multiplatform.common.types.testapp

import kotlinx.serialization.Serializable

@Serializable
data class TestSerializableClass(
    val name: String,
    val num: Int
)