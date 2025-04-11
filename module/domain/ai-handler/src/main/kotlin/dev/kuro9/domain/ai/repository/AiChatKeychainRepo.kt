package dev.kuro9.domain.ai.repository

import dev.kuro9.domain.ai.table.AiChatKeychains
import dev.kuro9.domain.database.fetchFirstOrNull
import org.jetbrains.exposed.sql.insert
import org.springframework.stereotype.Repository

@Repository
class AiChatKeychainRepo {

    fun findRootKey(refKey: String): String? {
        return AiChatKeychains.select(AiChatKeychains.rootKey)
            .where { AiChatKeychains.refKey eq refKey }
            .fetchFirstOrNull(AiChatKeychains.rootKey)
    }

    fun insertKey(rootKey: String, refKey: String, key: String) {
        AiChatKeychains.insert {
            it[this.rootKey] = rootKey
            it[this.refKey] = refKey
            it[this.key] = key
        }
    }
}