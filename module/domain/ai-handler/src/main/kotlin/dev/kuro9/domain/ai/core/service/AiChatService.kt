package dev.kuro9.domain.ai.core.service

import dev.kuro9.domain.ai.log.dto.AiChatLogConfigDto
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto

interface AiChatService {

    /**
     * @param systemInstruction 해당 봇을 정의하는 프롬프트 작성
     * @param input 사용자의 입력
     * @param tools 사용할 툴
     * @param userId 유저 식별자
     * @param key 현재 채팅 메시지 식별자
     * @param refKey 이전 채팅 메시지 식별자. 없다면 null
     * @param logConfig 유지할 로그 개수에 대한 config. 무제한 시 null
     *
     * @return AI의 응답
     */
    suspend fun doChat(
        systemInstruction: String,
        input: String,
        tools: List<GoogleAiToolDto> = emptyList(),
        userId: Long,
        key: String,
        refKey: String? = null,
        logConfig: AiChatLogConfigDto? = null,
    ): String
}