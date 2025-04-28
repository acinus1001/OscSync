package dev.kuro9.domain.ai.memory.repository

import dev.kuro9.domain.ai.memory.table.AiMasterMemories
import dev.kuro9.domain.ai.memory.table.AiMasterMemoryEntity
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.BatchUpdateStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class AiMasterMemoryRepo {

    fun findAll(userId: Long): List<AiMasterMemoryEntity> {
        return Op.Companion.build { AiMasterMemories.userId eq userId }
            .and { AiMasterMemories.revokedAt.isNull() }
            .let(AiMasterMemoryEntity.Companion::find)
            .orderBy(AiMasterMemories.id to org.jetbrains.exposed.sql.SortOrder.ASC)
            .toList()
    }

    suspend fun add(userId: Long, memory: String, sizeLimit: Int? = null) {
        val memoryList = findAll(userId)
        if (sizeLimit != null) run {

            // 메모리 오래된 거부터 지우기
            val deleteCount = memoryList.size - (sizeLimit - 1)

            if (deleteCount <= 0) return@run

            coroutineScope {
                launch {
                    BatchUpdateStatement(AiMasterMemories).apply {
                        memoryList.take(deleteCount).forEach {
                            addBatch(EntityID(it.id.value, AiMasterMemories))
                            this[AiMasterMemories.revokedAt] = LocalDateTime.now()
                        }
                    }.execute(TransactionManager.current())
                }
            }
        }

        AiMasterMemories.insert {
            it[this.userId] = userId
            it[this.memory] = memory
            it[this.createdAt] = LocalDateTime.now()
            it[this.revokedAt] = null
        }
    }

    fun revoke(userId: Long, memoryIndex: Long) {
        AiMasterMemories.update(where = { (AiMasterMemories.userId eq userId) and (AiMasterMemories.id eq memoryIndex) }) {
            it[this.revokedAt] = LocalDateTime.Companion.now()
        }
    }
}