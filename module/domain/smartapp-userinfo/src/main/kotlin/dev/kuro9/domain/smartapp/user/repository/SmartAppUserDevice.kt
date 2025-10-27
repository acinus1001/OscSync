package dev.kuro9.domain.smartapp.user.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object SmartAppUserDevices : CompositeIdTable("smartapp_user_device") {
    val userId = long("user_id")
    val deviceId = varchar("device_id", 100)
    val deviceComponent = varchar("device_component", 50)
    val deviceCapability = varchar("device_capability", 50)
    val deviceName = varchar("device_name", 50) // must be unique per user

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    private data object EntityId {
        val userId = SmartAppUserDevices.userId.entityId()
        val deviceId = SmartAppUserDevices.deviceId.entityId()
        val deviceComponent = SmartAppUserDevices.deviceComponent.entityId()
        val deviceCapability = SmartAppUserDevices.deviceCapability.entityId()
    }

    override val primaryKey = PrimaryKey(
        EntityId.userId,
        EntityId.deviceId,
        EntityId.deviceComponent,
        EntityId.deviceCapability
    )
}

class SmartAppUserDeviceEntity(pk: EntityID<CompositeID>) : CompositeEntity(pk) {
    companion object : CompositeEntityClass<SmartAppUserDeviceEntity>(SmartAppUserDevices)

    val userId by SmartAppUserDevices.userId
    val deviceId by SmartAppUserDevices.deviceId
    val deviceComponent by SmartAppUserDevices.deviceComponent
    val deviceCapability by SmartAppUserDevices.deviceCapability
    var deviceName by SmartAppUserDevices.deviceName; private set
    val createdAt by SmartAppUserDevices.createdAt
    var updatedAt by SmartAppUserDevices.updatedAt; private set

    fun updateName(name: String) {
        this.deviceName = name
        this.updatedAt = LocalDateTime.now()
    }
}