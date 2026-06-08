package dev.kuro9.domain.discord.name.service

import dev.kuro9.domain.discord.name.dto.DiscordIdAndName
import dev.kuro9.multiplatform.common.strings.disassembleHangul
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class DiscordSearchService(
    private val redisTemplate: StringRedisTemplate,
    private val cacheManager: CacheManager
) {
    companion object {
        private const val SEARCH_KEY = "cache-discord-search"
        private const val CACHE_NAME = "cache-discord-name"
    }

    /**
     * 유저의 디스코드 이름을 업데이트하고 기존 검색 데이터를 정제합니다.
     */
    fun updateDiscordName(userId: Long, newDiscordName: String) {
        val cache = cacheManager.getCache(CACHE_NAME)

        // 1. 기존 데이터 삭제 로직 (기존 캐시에서 이름을 가져와 풀어쓴 뒤 매칭되는 항목 제거)
        cache?.get(userId)?.get()?.let { oldName ->
            val oldDiscordName = oldName as String
            if (oldDiscordName != newDiscordName) {
                val oldDisassembled = disassembleHangul(oldDiscordName)
                val oldMember = "$oldDisassembled::$oldDiscordName::$userId"
                redisTemplate.opsForZSet().remove(SEARCH_KEY, oldMember)
            }
        }

        // 2. 새 데이터 풀어쓰기 후 저장
        val newDisassembled = disassembleHangul(newDiscordName)
        val newMember = "$newDisassembled::$newDiscordName::$userId" // 풀어쓴이름::원본이름::유저ID

        redisTemplate.opsForZSet().add(SEARCH_KEY, newMember, 0.0)
        cache?.put(userId, newDiscordName)
    }

    /**
     * 디스코드 이름 자동완성 검색
     */
    fun findByUsername(keyword: String, limit: Int = 10): List<DiscordIdAndName> {
        val disassembledKeyword = disassembleHangul(keyword)

        val lowerBound = Range.Bound.inclusive("[$disassembledKeyword")
        val upperBound = Range.Bound.inclusive("[${disassembledKeyword}\uffff")
        val range: Range<String> = Range.from(lowerBound).to(upperBound)
        val limit: Limit = Limit.limit().count(limit)

        // 최신 버전의 rangeByLex는 이 Range 객체를 그대로 받아들입니다.
        val searchResults = redisTemplate.opsForZSet().rangeByLex(SEARCH_KEY, range, limit) ?: emptySet()

        return searchResults.mapNotNull { item ->
            val parts = item.split("::")
            if (parts.size != 2) return@mapNotNull null

            DiscordIdAndName(name = parts[0], id = parts[1].toLong())
        }
    }
}