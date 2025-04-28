package dev.kuro9.domain.ai.core.service

import dev.kuro9.domain.ai.memory.service.AiMasterMemoryService
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import io.github.harryjhin.slf4j.extension.info
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Primary
@Service
class AiMemoryChatServiceProxy(
    @Qualifier("googleLoggedAiChatService") private val origin: AiChatService,
    private val memoryService: AiMasterMemoryService,
) : AiChatService {

    override suspend fun doChat(
        systemInstruction: String,
        input: String,
        tools: List<GoogleAiToolDto>,
        userId: Long,
        key: String,
        refKey: String?
    ): String {
        val memoryList = memoryService.findAllWithIndex(userId)
        val memoryString = memoryList.joinToString(
            separator = "\n",
            prefix = "현재 규칙(${memoryList.size}개/최대 개수 2개): \n",
            postfix = "\n\n"
        ) { (index, memory) -> "[$index] $memory" }

        info { memoryString }

        return origin.doChat(
            systemInstruction = systemInstruction,
            input = memoryString + input,
            tools = tools,
            userId = userId,
            key = key,
            refKey = refKey
        )
    }
}