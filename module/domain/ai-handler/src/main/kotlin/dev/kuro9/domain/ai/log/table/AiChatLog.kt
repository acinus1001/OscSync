package dev.kuro9.domain.ai.log.table

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object AiChatLogs : LongIdTable("ai_chat_log") {
    val key = varchar("key", 28).index()
    val rootKey = varchar("root_key", 28).index()
    val payload = text("payload")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val revokeAt = datetime("revoke_at").nullable()
    val userId = long("user_id").nullable().index()
}

class AiChatLogEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<AiChatLogEntity>(AiChatLogs)

    var key by AiChatLogs.key
    var rootKey by AiChatLogs.rootKey
    var payload by AiChatLogs.payload
    var createdAt by AiChatLogs.createdAt
    var revokeAt by AiChatLogs.revokeAt
    var userId by AiChatLogs.userId
}

data class AiChatLog(
    val key: String,
    val rootKey: String,
    val payload: String,
    val userId: Long,
)