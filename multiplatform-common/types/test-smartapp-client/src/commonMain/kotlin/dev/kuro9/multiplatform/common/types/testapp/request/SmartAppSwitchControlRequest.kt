package dev.kuro9.multiplatform.common.types.testapp.request

import kotlinx.serialization.Serializable

@Serializable
data class SmartAppSwitchControlRequest(
    val deviceId: String,
    val value: Boolean,
)
