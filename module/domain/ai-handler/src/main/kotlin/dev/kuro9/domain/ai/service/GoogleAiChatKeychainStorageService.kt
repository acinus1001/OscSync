package dev.kuro9.domain.ai.service

import com.google.genai.types.Content
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

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

        return storage[rootKey ?: nowKey] ?: emptyList()
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