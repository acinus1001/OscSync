package dev.kuro9.domain.ai.core.service

import dev.kuro9.domain.ai.log.service.GoogleAiChatKeychainStorageService
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import dev.kuro9.internal.google.ai.service.GoogleAiService
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service

@Service
class GoogleLoggedAiChatService(
    private val aiService: GoogleAiService,
    private val logStorage: GoogleAiChatKeychainStorageService,
) : AiChatService {

    override suspend fun doChat(
        systemInstruction: String,
        input: String,
        tools: List<GoogleAiToolDto>,
        userId: Long,
        key: String,
        refKey: String?
    ): String {
        val log = logStorage.get(refKey, key)
        info { "log count: ${log.size}" }

        val (result, sessionLog) = aiService.chat(
            systemInstruction = systemInstruction,
            input = input,
            tools = tools,
            chatLog = log
        )
        logStorage.append(userId, key, sessionLog)
        return result
    }
}