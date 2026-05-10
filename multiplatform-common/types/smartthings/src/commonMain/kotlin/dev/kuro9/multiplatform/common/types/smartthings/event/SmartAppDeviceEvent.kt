package dev.kuro9.multiplatform.common.types.smartthings.event

data class SmartAppDeviceEvent(
    val capability: String,
    val componentId: String,
    val deviceId: String,
    val state: Boolean,
)