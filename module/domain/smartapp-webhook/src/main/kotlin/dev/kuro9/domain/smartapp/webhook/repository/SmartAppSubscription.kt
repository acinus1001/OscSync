package dev.kuro9.domain.smartapp.webhook.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object SmartAppSubscriptions : LongIdTable("smartapp_subscription") {
    val appId = varchar("app_id", 255)
    var subscriptionId = varchar("subscription_id", 255)
    val deviceId = varchar("device_id", 255)
    val authToken = varchar("auth_token", 255)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

class SmartAppSubscriptionEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<SmartAppSubscriptionEntity>(SmartAppSubscriptions)

    var appId by SmartAppSubscriptions.appId
    var subscriptionId by SmartAppSubscriptions.subscriptionId
    var deviceId by SmartAppSubscriptions.deviceId
    var authToken by SmartAppSubscriptions.authToken
    var createdAt by SmartAppSubscriptions.createdAt
}
