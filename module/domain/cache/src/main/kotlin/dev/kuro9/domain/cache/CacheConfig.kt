package dev.kuro9.domain.cache

import dev.kuro9.domain.cache.serializer.KotlinRedisProtoBufSerializer
import kotlinx.serialization.serializer
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    @ConditionalOnMissingBean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager()
    }

    @Bean
    fun redisCacheManager(
        connectionFactory: RedisConnectionFactory,
        redisCacheCustomizer: RedisCacheManagerBuilderCustomizer
    ): CacheManager {

        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(1.minutes.toJavaDuration())
            .disableCachingNullValues()

        val builder = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(redisCacheConfiguration)

        redisCacheCustomizer.customize(builder)

        return builder.build()
    }

    @Bean
    fun redisCacheCustomizer(): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
//            builder.withCacheConfiguration("example", typedCacheConfiguration<String>())

            builder.withCacheConfiguration("cache-1m", jdkCacheConfiguration(1.minutes))
            builder.withCacheConfiguration("cache-5m", jdkCacheConfiguration(5.minutes))
            builder.withCacheConfiguration("cache-10m", jdkCacheConfiguration(10.minutes))
            builder.withCacheConfiguration("cache-1h", jdkCacheConfiguration(1.hours))
            builder.withCacheConfiguration("cache-1d", jdkCacheConfiguration(1.days))
        }
    }

    private fun jdkCacheConfiguration(ttl: Duration): RedisCacheConfiguration {

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl.toJavaDuration())
            .disableCachingNullValues()
            .serializeValuesWith(fromSerializer(JdkSerializationRedisSerializer()))

    }

    private inline fun <reified T : Any> typedCacheConfiguration(ttl: Duration = 1.minutes): RedisCacheConfiguration {
        return KotlinRedisProtoBufSerializer(serializer<T>())
            .let(::fromSerializer)
            .let(RedisCacheConfiguration.defaultCacheConfig()::serializeValuesWith)
            .disableCachingNullValues()
            .entryTtl(ttl.toJavaDuration())
    }
}