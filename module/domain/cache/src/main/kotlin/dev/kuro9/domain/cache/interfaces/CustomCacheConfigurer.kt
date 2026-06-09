package dev.kuro9.domain.cache.interfaces

import org.springframework.data.redis.cache.RedisCacheConfiguration

interface CustomCacheConfigurer {
    fun getCacheName(): String
    fun getCacheConfiguration(): RedisCacheConfiguration
}