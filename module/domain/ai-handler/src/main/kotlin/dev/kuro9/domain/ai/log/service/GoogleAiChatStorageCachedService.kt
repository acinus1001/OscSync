package dev.kuro9.domain.ai.log.service

import com.google.genai.types.Content
import io.github.harryjhin.slf4j.extension.info
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@[Primary Service]
class GoogleAiChatStorageCachedService(
    @Qualifier("googleAiChatStorageService") private val origin: GoogleAiChatStorage,
    private val cacheManager: CacheManager,
) : GoogleAiChatStorage {

    override fun get(rootKey: String): List<Content>? {

        @Suppress("UNCHECKED_CAST")
        return (cacheManager.getCache("ai-chat-log")
            ?.get(rootKey, List::class.java) as? List<Content>)
            ?.also { info { "chatStorage#get cache hit for $rootKey" } }
            ?: origin[rootKey]
                .also { info { "chatStorage#get cache miss for $rootKey" } }
    }

    override fun append(userId: Long, key: String, rootKey: String, log: List<Content>) {

        @Suppress("UNCHECKED_CAST")
        val cachedData = cacheManager.getCache("ai-chat-log")
            ?.get(rootKey, List::class.java) as? List<Content>? ?: origin[rootKey] ?: emptyList()

        cacheManager.getCache("ai-chat-log")?.put(rootKey, cachedData + log)
        origin.append(userId, key, rootKey, log)
    }

    override fun remove(rootKey: String) {
        cacheManager.getCache("ai-chat-log")?.evict(rootKey)
        origin.remove(rootKey)
    }

    override fun drop(key: String, count: Int) {
        cacheManager.getCache("ai-chat-log")?.evict(key)
        origin.drop(key, count)
    }
}