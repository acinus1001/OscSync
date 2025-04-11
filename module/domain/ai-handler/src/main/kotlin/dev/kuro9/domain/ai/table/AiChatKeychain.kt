package dev.kuro9.domain.ai.table

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object AiChatKeychains : LongIdTable("ai_chat_keychain", "seq") {
    val rootKey = varchar("root_key", 28).index()
    val refKey = varchar("refKey", 28).index()
    val key = varchar("key", 28).uniqueIndex()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

class AiChatKeychainEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AiChatKeychainEntity>(AiChatKeychains)

    var rootKey by AiChatKeychains.rootKey
    var refKey by AiChatKeychains.refKey
    var key by AiChatKeychains.key
    var createdAt by AiChatKeychains.createdAt
}