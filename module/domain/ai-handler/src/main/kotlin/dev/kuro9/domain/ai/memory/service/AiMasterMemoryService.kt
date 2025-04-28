package dev.kuro9.domain.ai.memory.service

import dev.kuro9.domain.ai.memory.repository.AiMasterMemoryRepo
import dev.kuro9.domain.ai.memory.table.AiMasterMemoryEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AiMasterMemoryService(private val memoryRepo: AiMasterMemoryRepo) {

    fun findAll(userId: Long): List<String> {
        return memoryRepo.findAll(userId).map(AiMasterMemoryEntity::memory)
    }

    fun findAllWithIndex(userId: Long): List<Pair<Long, String>> {
        return memoryRepo.findAll(userId).map {
            it.id.value to it.memory
        }
    }

    @Transactional
    suspend fun add(userId: Long, memory: String, sizeLimit: Int? = null): Job {
        return coroutineScope {
            launch {
                memoryRepo.add(userId, memory.take(100), sizeLimit)
            }
        }
    }

    @Transactional
    suspend fun revoke(userId: Long, memoryIndex: Long): Job {
        return coroutineScope {
            launch {
                memoryRepo.revoke(userId, memoryIndex)
            }
        }
    }
}