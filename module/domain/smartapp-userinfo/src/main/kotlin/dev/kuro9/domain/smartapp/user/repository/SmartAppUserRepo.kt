package dev.kuro9.domain.smartapp.user.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.upsert
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class SmartAppUserRepo {

    @Transactional
    fun registerDevice(
        userId: Long,
        deviceId: String,
        deviceComponentId: String,
        deviceCapabilityId: String,
        deviceName: String,
    ) {
        SmartAppUserDevices.upsert {
            it[SmartAppUserDevices.userId] = userId
            it[SmartAppUserDevices.deviceId] = deviceId
            it[SmartAppUserDevices.deviceComponent] = deviceComponentId
            it[SmartAppUserDevices.deviceCapability] = deviceCapabilityId
            it[SmartAppUserDevices.deviceName] = deviceName
        }
    }

    /**
     * deviceName으로 사용자 기기 삭제
     *
     * @return 삭제 row 존재 여부
     */
    @Transactional
    fun deleteDeviceByName(userId: Long, deviceName: String): Boolean {
        return (SmartAppUserDevices.userId eq userId)
            .and { SmartAppUserDevices.deviceName eq deviceName }
            .let { op -> SmartAppUserDevices.deleteWhere { op } } != 0

    }

    fun getRegisteredDeviceByName(
        userId: Long,
        deviceName: String,
    ): SmartAppUserDeviceEntity? {
        return (SmartAppUserDevices.userId eq userId)
            .and { SmartAppUserDevices.deviceName eq deviceName }
            .and { SmartAppUserDevices.deviceComponent eq "main" }
            .and { SmartAppUserDevices.deviceCapability eq "switch" }
            .let(SmartAppUserDeviceEntity::find)
            .singleOrNull()
    }

    fun getRegisteredDevices(userId: Long): List<SmartAppUserDevice> {
        return SmartAppUserDeviceEntity
            .find { SmartAppUserDevices.userId eq userId }
            .notForUpdate()
            .toList()
            .map { it.toDto() }
    }
}