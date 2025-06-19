package dev.kuro9.domain.ai.core.service

interface AiSearchService {
    suspend fun search(query: String): String
}