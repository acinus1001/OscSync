package dev.kuro9.domain.ai.log.repository

import dev.kuro9.domain.ai.log.table.AiChatKeychains
import dev.kuro9.domain.database.fetchFirstOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.orWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Repository

@Repository
class AiChatKeychainRepo {

    fun findRootKey(key: String): String? {
        return AiChatKeychains.select(AiChatKeychains.rootKey)
            .where { AiChatKeychains.key eq key }
            .orWhere { AiChatKeychains.refKey eq key }
            .fetchFirstOrNull(AiChatKeychains.rootKey)
    }

    fun insertKey(rootKey: String, refKey: String, key: String) {
        AiChatKeychains.insertIgnore {
            it[this.rootKey] = rootKey
            it[this.refKey] = refKey
            it[this.key] = key
        }
    }
}