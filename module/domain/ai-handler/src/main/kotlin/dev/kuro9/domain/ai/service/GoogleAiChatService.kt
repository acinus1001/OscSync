package dev.kuro9.domain.ai.service

import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import dev.kuro9.internal.google.ai.service.GoogleAiService
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service

@Service
class GoogleAiChatService(
    private val aiService: GoogleAiService,
    private val logStorage: GoogleAiChatKeychainStorageService
) {

    suspend fun chatWithLog(
        systemInstruction: String,
        input: String,
        tools: List<GoogleAiToolDto>,
        key: String,
        refKey: String? = null,
    ): String {
        val log = logStorage.get(refKey, key)
        info { "log count: ${log.size}" }

        val (result, sessionLog) = aiService.chat(
            systemInstruction = systemInstruction,
            input = input,
            tools = tools,
            chatLog = log
        )
        logStorage.append(key, sessionLog)
        return result
    }

    suspend fun search(
        input: String,
    ) = aiService.search(input)
}