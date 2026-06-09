package dev.kuro9.domain.discord.name.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.Resource
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStringCommands
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.types.Expiration
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
class DiscordCacheInitService(
    private val connectionFactory: RedisConnectionFactory,
    @param:Value("classpath:name.csv") private val csvData: Resource,
    private val discordSearchService: DiscordSearchService,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("🚀 단발성 Redis 캐시 적재를 시작합니다... (Jackson 안씀)")

        try {
            // 1. 여기서 직접 RedisTemplate을 만들어 세팅합니다.
            val redisTemplate = RedisTemplate<String, Any>().apply {
                this.connectionFactory = this@DiscordCacheInitService.connectionFactory
                // 기존 코드를 보니 jdkCacheConfiguration을 쓰시므로 Value는 JDK 직렬화일 확률이 높습니다.
                this.keySerializer = StringRedisSerializer()
                this.valueSerializer = JdkSerializationRedisSerializer()
                this.afterPropertiesSet()
            }

            // 2. Serializer 준비 (기존 @Cacheable 설정 직렬화 방식 그대로 추적)
            @Suppress("UNCHECKED_CAST")
            val keySerializer = redisTemplate.keySerializer as RedisSerializer<Any>

            @Suppress("UNCHECKED_CAST")
            val valueSerializer = redisTemplate.valueSerializer as RedisSerializer<Any>

            // 3. 파일 읽기 시작
            csvData.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->

                    // 첫 줄(id,name 헤더)은 그냥 읽어서 버림
                    val header = reader.readLine()
                    if (header == null) {
                        println("⚠️ CSV 파일이 비어있습니다.")
                        return
                    }

                    // 4. Redis Pipelining으로 초고속 적재
                    redisTemplate.executePipelined { connection ->
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            // 공백 제거 및 쉼표 분리
                            val tokens = line!!.split(",").map { it.trim() }

                            // id와 name이 정상적으로 존재하는 라인만 처리
                            if (tokens.size >= 2) {
                                val userId = tokens[0]
                                val name = tokens[1]

                                if (userId.isNotEmpty() && name.isNotEmpty()) {
                                    // @Cacheable(value = ["cache-discord-name"], key = "#userId") 네이밍 매칭
                                    val redisKey = "cache-discord-name::$userId"

                                    val rawKey = keySerializer.serialize(redisKey)
                                    val rawValue = valueSerializer.serialize(name)

                                    if (rawKey != null && rawValue != null) {
                                        connection.stringCommands().set(
                                            rawKey,
                                            rawValue,
                                            Expiration.persistent(),
                                            RedisStringCommands.SetOption.SET_IF_ABSENT
                                        )
                                        discordSearchService.updateDiscordName(userId.toLong(), name)
                                    }
                                }
                            }
                        }
                        null // executePipelined는 Lambda의 반환값이 null이어야 함
                    }
                }
            }
            println("✅ Redis 캐시 적재가 완료되었습니다!")

        } catch (e: Exception) {
            println("❌ 캐시 적재 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
    }
}