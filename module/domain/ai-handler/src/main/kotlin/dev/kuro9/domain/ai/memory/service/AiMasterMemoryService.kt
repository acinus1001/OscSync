package dev.kuro9.domain.ai.memory.service

import dev.kuro9.domain.ai.memory.repository.AiMasterMemoryRepo
import dev.kuro9.domain.ai.memory.table.AiMasterMemoryEntity
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AiMasterMemoryService(private val memoryRepo: AiMasterMemoryRepo) {

    @Cacheable("ai-master-memory-list", key = "#userId")
    fun findAll(userId: Long): List<String> {
        info { "ai-master-memory-list for user $userId" }
        return memoryRepo.findAll(userId).map(AiMasterMemoryEntity::memory)
    }

    @Cacheable("ai-master-memory-list-w-i", key = "#userId")
    fun findAllWithIndex(userId: Long): List<Pair<Long, String>> {
        info { "ai-master-memory-list-w-i for user $userId" }
        return memoryRepo.findAll(userId).map {
            it.id.value to it.memory
        }
    }

    @Transactional
    @CacheEvict(cacheNames = ["ai-master-memory-list", "ai-master-memory-list-w-i"], key = "#userId")
    suspend fun add(userId: Long, memory: String, sizeLimit: Int? = null): Job {
        return coroutineScope {
            launch {
                memoryRepo.add(userId, memory.take(100), sizeLimit)
            }
        }
    }

    @Transactional
    @CacheEvict(cacheNames = ["ai-master-memory-list", "ai-master-memory-list-w-i"], key = "#userId")
    suspend fun revoke(userId: Long, memoryIndex: Long): Job {
        return coroutineScope {
            launch {
                memoryRepo.revoke(userId, memoryIndex)
            }
        }
    }
}