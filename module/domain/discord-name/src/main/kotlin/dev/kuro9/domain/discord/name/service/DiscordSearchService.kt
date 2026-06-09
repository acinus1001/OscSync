package dev.kuro9.domain.discord.name.service

import dev.kuro9.domain.discord.name.dto.DiscordIdAndName
import dev.kuro9.multiplatform.common.strings.disassembleHangul
import io.github.harryjhin.slf4j.extension.info
import jakarta.annotation.PostConstruct
import org.springframework.cache.CacheManager
import org.springframework.cache.get
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.Limit
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class DiscordSearchService(
    private val redisTemplate: StringRedisTemplate,
    private val cacheManager: CacheManager,

    ) {
    companion object {
        private const val SEARCH_KEY = "cache-discord-search"
        private const val CACHE_NAME = "cache-discord-name"
    }

    fun findById(userId: Long): DiscordIdAndName? {
        val cache = cacheManager.getCache(CACHE_NAME)
        return cache?.get<String>(userId)?.let { DiscordIdAndName(userId, it) }
    }

    fun findByUsername(keyword: String, limit: Int = 10): List<DiscordIdAndName> {
        if (keyword.isEmpty()) return emptyList()

        val disassembledKeyword = disassembleHangul(keyword).lowercase()

        val lowerBound = Range.Bound.inclusive(disassembledKeyword)
        val upperBound = Range.Bound.exclusive("${disassembledKeyword.dropLast(1)}${disassembledKeyword.last() + 1}")
        val range: Range<String> = Range.from(lowerBound).to(upperBound)
        val limit: Limit = Limit.limit().count(limit)

        info { "range : $range" }

        val searchResults = redisTemplate.opsForZSet().rangeByLex(SEARCH_KEY, range, limit) ?: emptySet()

        info { "searchResults : $searchResults" }

        return searchResults.mapNotNull { item ->
            val parts = item.split("::")
            if (parts.size != 3) return@mapNotNull null

            DiscordIdAndName(name = parts[1], id = parts[2].toLong())
        }
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
                val oldDisassembled = disassembleHangul(oldDiscordName).lowercase()
                val oldMember = "$oldDisassembled::$oldDiscordName::$userId"
                redisTemplate.opsForZSet().remove(SEARCH_KEY, oldMember)
            }
        }

        // 2. 새 데이터 풀어쓰기 후 저장
        val newDisassembled = disassembleHangul(newDiscordName).lowercase()
        val newMember = "$newDisassembled::$newDiscordName::$userId" // 풀어쓴이름::원본이름::유저ID

        redisTemplate.opsForZSet().add(SEARCH_KEY, newMember, 0.0)
        cache?.put(userId, newDiscordName)
    }

    private fun internalIndexingAllData() {
        val stringRedisTemplate = redisTemplate
        val destinationZSetKey = SEARCH_KEY
        val cacheName = CACHE_NAME

        // 1. 목적지인 검색용 ZSet Key 초기화 (순수 문자열 기반)
        stringRedisTemplate.delete(destinationZSetKey)
        val opsForZSet = stringRedisTemplate.opsForZSet()

        // 2. CacheManager에서 기존 JDK 기반 캐시 저장소 꺼내기
        val targetCache = cacheManager.getCache(cacheName)
            ?: throw IllegalStateException("[$cacheName] 캐시 설정을 찾을 수 없습니다.")

        // 3. 패턴을 사용해 Redis에서 '순수 문자열 키' 전체 스캔
        val cacheKeyPattern = "$cacheName::*"
        val scanOptions = ScanOptions.scanOptions().match(cacheKeyPattern).count(1000).build()
        val cursor = stringRedisTemplate.scan(scanOptions)

        var migrationCount = 0

        cursor.use { c ->
            while (c.hasNext()) {
                val fullCacheKey = c.next() // 예: "cache-discord-name::12345678"

                // 스프링 캐시 내부에 매핑할 때는 앞의 '캐시이름::'을 뗀 순수 'userId'가 key 규칙입니다.
                val userId = fullCacheKey.substringAfter("$cacheName::")

                // 🌟 4. 핵심: CacheManager를 통해 데이터를 안전하게 읽어옵니다.
                // 내부적으로 JdkSerializationRedisSerializer가 돌면서 온전한 String 객체로 복원해 줍니다.
                val discordName = targetCache.get(userId, String::class.java) ?: continue

                // 5. 자소 분리 및 소문자화 진행
                val disassembled = disassembleHangul(discordName)
                val value1 = disassembled.lowercase()
                val value2 = discordName
                val value3 = userId

                val zsetMember = "$value1::$value2::$value3"

                // 6. 목적지 ZSet에 저장 (StringRedisTemplate이므로 깨짐 없이 깔끔하게 저장됩니다)
                opsForZSet.add(destinationZSetKey, zsetMember, 0.0)
                migrationCount++
            }
        }
        println("✅ 캐시 마이그레이션 완료! 총 ${migrationCount}개의 캐시 데이터를 기반으로 [$SEARCH_KEY] ZSet을 새로 생성했습니다.")
    }

    @PostConstruct
    fun init() {
        internalIndexingAllData()
    }
}