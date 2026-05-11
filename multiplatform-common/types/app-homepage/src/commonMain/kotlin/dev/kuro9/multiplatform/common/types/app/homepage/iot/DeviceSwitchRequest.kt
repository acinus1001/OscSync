package dev.kuro9.multiplatform.common.types.app.homepage.iot

import kotlinx.serialization.Serializable

@Serializable
data class DeviceSwitchRequest(val desireState: Boolean)
