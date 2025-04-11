package dev.kuro9.domain.ai.service

import dev.kuro9.domain.ai.repository.AiChatKeychainRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@[Transactional(readOnly = true) Service]
class GoogleAiKeychainStorageService(private val repo: AiChatKeychainRepo) : GoogleAiKeychainStorage {
    override fun getRootKey(key: String): String? {
        return repo.findRootKey(key)
    }

    @Transactional
    override fun addKeychain(rootKey: String, refKey: String, key: String) {
        return repo.insertKey(rootKey, refKey, key)
    }
}