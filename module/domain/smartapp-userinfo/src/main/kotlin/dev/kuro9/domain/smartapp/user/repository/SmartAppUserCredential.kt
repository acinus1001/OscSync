package dev.kuro9.domain.smartapp.user.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.crypt.Algorithms
import org.jetbrains.exposed.v1.crypt.encryptedVarchar
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object SmartAppUserCredentials : LongIdTable("smartapp_user_credential", "user_id") {
    val userId by ::id
    val smartAppToken = encryptedVarchar(
        name = "smartapp_token",
        cipherTextLength = 512,
        encryptor = Algorithms.AES_256_PBE_GCM(
            password = System.getenv("COL_AES_KEY"),
            salt = System.getenv("COL_AES_SALT"),
        )
    )
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class SmartAppUserCredentialEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SmartAppUserCredentialEntity>(SmartAppUserCredentials)

    val userId by SmartAppUserCredentials.userId
    var smartAppToken by SmartAppUserCredentials.smartAppToken; private set
    val createdAt by SmartAppUserCredentials.createdAt
    var updatedAt by SmartAppUserCredentials.updatedAt; private set

    fun updateToken(token: String) {
        this.smartAppToken = token
        this.updatedAt = LocalDateTime.now()
    }
}