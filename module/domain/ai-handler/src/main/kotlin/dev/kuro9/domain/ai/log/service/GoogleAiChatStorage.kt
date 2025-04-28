package dev.kuro9.domain.ai.log.service

import com.google.genai.types.Content

interface GoogleAiChatStorage {
    operator fun get(rootKey: String): List<Content>?
    fun append(userId: Long, key: String, rootKey: String, log: List<Content>)
    fun drop(key: String, count: Int)
    fun remove(rootKey: String)
}