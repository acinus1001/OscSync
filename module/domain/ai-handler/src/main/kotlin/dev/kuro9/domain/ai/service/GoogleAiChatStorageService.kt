package dev.kuro9.domain.ai.service

import com.google.genai.types.Content
import dev.kuro9.domain.ai.repository.AiChatLogRepo
import dev.kuro9.domain.ai.table.AiChatLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GoogleAiChatStorageService(private val repo: AiChatLogRepo) : GoogleAiChatStorage {

    override fun get(rootKey: String): List<Content>? {
        return repo.findAllByRootKey(rootKey).map {
            Content.fromJson(it.payload)
        }
    }

    @Transactional
    override fun append(userId: Long, key: String, rootKey: String, log: List<Content>) {
        log.map { AiChatLog(key = key, rootKey = rootKey, payload = it.toJson(), userId = userId) }
            .let(repo::saveAll)
    }

    @Transactional
    override fun drop(key: String, count: Int) {
        val idList = repo.findAllId(key, count)
        repo.revokeAll(idList)
    }

    @Transactional
    override fun remove(rootKey: String) {
        val idList = repo.findAllId(rootKey)
        repo.revokeAll(idList)
    }


}