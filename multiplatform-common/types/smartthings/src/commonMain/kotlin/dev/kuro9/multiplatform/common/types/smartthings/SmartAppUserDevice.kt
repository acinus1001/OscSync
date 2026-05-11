package dev.kuro9.multiplatform.common.types.smartthings

import dev.kuro9.multiplatform.common.types.smartthings.serialize.PlatformSerializable
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SmartAppUserDevice(
    val userId: Long,
    val deviceId: String,
    val deviceComponent: String,
    val deviceCapability: String,
    val deviceName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) : PlatformSerializable