package dev.kuro9.domain.ai.memory.table

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object AiMasterMemories : LongIdTable("ai_master_memory") {
    val userId = long("user_id")
    val memory = varchar("memory", 100)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val revokedAt = datetime("revoked_at").nullable()
}

class AiMasterMemoryEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<AiMasterMemoryEntity>(AiMasterMemories)

    val userId by AiMasterMemories.userId
    val memory by AiMasterMemories.memory
    val createdAt by AiMasterMemories.createdAt
    val revokedAt by AiMasterMemories.revokedAt
}