package dev.kuro9.domain.smartapp.user.repository

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object SmartAppUserCredentials : LongIdTable("smartapp_user_credential", "user_id") {
    val userId by ::id
    val smartAppToken = varchar("smartapp_token", 50)
}

class SmartAppUserCredentialEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SmartAppUserCredentialEntity>(SmartAppUserCredentials)

    val userId by SmartAppUserCredentials.userId
    var smartAppToken by SmartAppUserCredentials.smartAppToken
        internal set
}