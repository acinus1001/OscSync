package dev.kuro9.internal.google.ai.dto

import com.google.genai.types.Content

data class GoogleAiChatResponse(
    val result: String,
    val sessionChatLog: List<Content>
)