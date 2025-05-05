package dev.kuro9.domain.ai.log.dto

data class AiChatLogConfigDto(
    val limitChatCount: Int = 350,
    val deboundCount: Int = 50,
)
