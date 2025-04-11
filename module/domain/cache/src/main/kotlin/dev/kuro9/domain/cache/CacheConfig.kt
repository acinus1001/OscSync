package dev.kuro9.domain.cache

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    @ConditionalOnMissingBean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager()
    }
}