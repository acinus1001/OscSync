package dev.kuro9.domain.ai.core.service

import dev.kuro9.domain.ai.log.dto.AiChatLogConfigDto
import dev.kuro9.domain.ai.memory.service.AiMasterMemoryService
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        refKey: String?,
        logConfig: AiChatLogConfigDto?,
    ): String {
        val memoryList = withContext(Dispatchers.IO) {
            memoryService.findAllWithIndex(userId)
        }

        val memoryString = memoryList.joinToString(
            separator = "\n",
            prefix = "유저에 대한 전역 메모리(${memoryList.size}개/최대 개수 10개): \n",
            postfix = "\n\n"
        ) { (index, memory) -> "$index. $memory" }

        info { memoryString }

        return origin.doChat(
            systemInstruction = systemInstruction + memoryString,
            input = input,
            tools = tools,
            userId = userId,
            key = key,
            refKey = refKey
        )
    }
}