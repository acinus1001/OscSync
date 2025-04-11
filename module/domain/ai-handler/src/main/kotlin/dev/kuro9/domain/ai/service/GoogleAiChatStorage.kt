package dev.kuro9.domain.ai.service

import com.google.genai.types.Content

interface GoogleAiChatStorage {
    operator fun get(key: String): List<Content>?
    fun append(key: String, log: List<Content>)
    fun drop(key: String, count: Int)
    fun remove(key: String)
}