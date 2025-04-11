package dev.kuro9.domain.ai.service

import com.google.genai.types.Content

interface GoogleAiChatStorage {
    operator fun get(rootKey: String): List<Content>?
    fun append(key: String, rootKey: String, log: List<Content>)
    fun drop(key: String, count: Int)
    fun remove(key: String)
}