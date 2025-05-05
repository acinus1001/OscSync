package dev.kuro9.domain.ai.log.service

import com.google.genai.types.Content
import dev.kuro9.domain.ai.log.dto.AiChatLogConfigDto
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleAiChatKeychainStorageService(
    private val storage: GoogleAiChatStorage,
    private val keyChainStorage: GoogleAiKeychainStorage,
) {

    suspend fun get(refKey: String?, nowKey: String, logConfig: AiChatLogConfigDto?): List<Content> {
        val rootKey = refKey?.let(keyChainStorage::getRootKey)

        coroutineScope {
            launch {
                when (rootKey) {
                    null -> keyChainStorage.addKeychain(nowKey, nowKey, nowKey)
                    else -> keyChainStorage.addKeychain(rootKey, refKey, nowKey)
                }
            }
        }

        val list = storage[rootKey ?: nowKey] ?: emptyList()
        if (logConfig != null && list.size > logConfig.limitChatCount) {
            val result = list.takeLast(logConfig.limitChatCount - logConfig.deboundCount)
            val toDrop = result.indexOfFirst { it.hasValidUserText() }
                .takeIf { it >= 0 }
                ?: 0

            coroutineScope { launch { storage.drop(rootKey ?: nowKey, list.size - 300 + toDrop) } }
            return result.drop(toDrop)
        }
        return list
    }

    suspend fun append(userId: Long, key: String, log: List<Content>) {
        coroutineScope {
            launch {
                val rootKey = keyChainStorage.getRootKey(key)
                storage.append(userId, key, rootKey ?: key, log)
            }
        }
    }

    private fun Content.hasValidUserText(): Boolean {
        val userRole = role().getOrNull() == "user"
        val hasNonNullText: Boolean by lazy {
            parts()
                .getOrDefault(emptyList())
                .any { part -> part.text().getOrNull() != null }
        }

        return userRole && hasNonNullText
    }
}