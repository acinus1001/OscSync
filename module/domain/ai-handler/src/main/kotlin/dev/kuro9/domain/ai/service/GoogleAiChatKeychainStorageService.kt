package dev.kuro9.domain.ai.service

import com.google.genai.types.Content
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleAiChatKeychainStorageService(
    private val storage: GoogleAiChatStorage,
    private val keyChainStorage: GoogleAiKeychainStorage,
) {

    suspend fun get(refKey: String?, nowKey: String): List<Content> {
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
        if (list.size > 200) {
            val result = list.takeLast(200)
            val toDrop = result.indexOfFirst { it.role().getOrNull() == "user" }
                .takeIf { it >= 0 }
                ?: 0

            coroutineScope { launch { storage.drop(rootKey ?: nowKey, list.size - 200 + toDrop) } }
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

    private val context = CoroutineName("GoogleAiServiceDatabase") + Dispatchers.IO
}