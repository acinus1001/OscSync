package dev.kuro9.domain.ai.log.repository

import dev.kuro9.domain.ai.log.table.AiChatLog
import dev.kuro9.domain.ai.log.table.AiChatLogEntity
import dev.kuro9.domain.ai.log.table.AiChatLogs
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Repository

@Repository
class AiChatLogRepo {

    fun findAll(key: String, limit: Int? = null): SizedIterable<AiChatLogEntity> {
        return Op.build { AiChatLogs.key eq key }
            .and { AiChatLogs.revokeAt.isNull() }
            .let(AiChatLogEntity::find)
            .orderBy(AiChatLogs.id to SortOrder.ASC)
            .apply { if (limit != null) limit(limit) }
    }

    fun findAllByRootKey(rootKey: String, limit: Int? = null): SizedIterable<AiChatLogEntity> {
        return Op.build { AiChatLogs.rootKey eq rootKey }
            .and { AiChatLogs.revokeAt.isNull() }
            .let(AiChatLogEntity::find)
            .orderBy(AiChatLogs.id to SortOrder.ASC)
            .apply { if (limit != null) limit(limit) }
    }

    fun findAllId(key: String, limit: Int? = null): List<Long> {
        return AiChatLogs.select(AiChatLogs.id)
            .where { AiChatLogs.key eq key }
            .andWhere { AiChatLogs.revokeAt.isNull() }
            .orderBy(AiChatLogs.id to SortOrder.ASC)
            .apply { if (limit != null) limit(limit) }
            .map { it[AiChatLogs.id].value }
    }

    fun findAllIdByRootKey(rootKey: String, limit: Int? = null): List<Long> {
        return AiChatLogs.select(AiChatLogs.id)
            .where { AiChatLogs.rootKey eq rootKey }
            .andWhere { AiChatLogs.revokeAt.isNull() }
            .orderBy(AiChatLogs.id to SortOrder.ASC)
            .apply { if (limit != null) limit(limit) }
            .map { it[AiChatLogs.id].value }
    }

    fun saveAll(logs: List<AiChatLog>) {
        AiChatLogs.batchInsert(logs) { log ->
            this[AiChatLogs.key] = log.key
            this[AiChatLogs.rootKey] = log.rootKey
            this[AiChatLogs.payload] = log.payload
            this[AiChatLogs.createdAt] = LocalDateTime.now()
            this[AiChatLogs.revokeAt] = null
            this[AiChatLogs.userId] = log.userId
        }
    }

    fun revokeAll(idSet: List<Long>) {
        if (idSet.isEmpty()) return
        BatchUpdateStatement(AiChatLogs).apply {
            idSet.forEach { id ->
                addBatch(EntityID(id, AiChatLogs))
                this[AiChatLogs.revokeAt] = LocalDateTime.now()
            }
        }.execute(TransactionManager.current())
    }
}