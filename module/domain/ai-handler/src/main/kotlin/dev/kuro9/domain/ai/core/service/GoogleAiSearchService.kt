package dev.kuro9.domain.ai.core.service

import dev.kuro9.internal.google.ai.service.GoogleAiService
import org.springframework.stereotype.Service

@Service
class GoogleAiSearchService(private val aiService: GoogleAiService) : AiSearchService {

    private val systemInstruction = """
            당신은 다른 자동화된 봇을 위한 검색 결과 제공 서비스입니다. 
            최대한 짧고 간결하게 응답을 요약해 제공하십시오.
            반드시 영미 단위계(인치, 화씨, 파운드, 온스 등) 로 응답을 변환하여 제공하십시오.
        """.trimIndent()

    override suspend fun search(query: String) = aiService.search(
        systemInstruction = systemInstruction,
        query = query
    )
}