package dev.kuro9.domain.mahjong.core.config

import dev.kuro9.domain.cache.CacheConfig.Companion.protoBufCacheConfiguration
import dev.kuro9.domain.cache.interfaces.CustomCacheConfigurer
import dev.kuro9.domain.mahjong.core.dto.MahjongGuildStat
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import kotlin.time.Duration.Companion.hours

@Configuration
class MahjongCacheConfig {

    @Bean
    fun mahjongCacheConfigurer(): CustomCacheConfigurer = object : CustomCacheConfigurer {
        override fun getCacheName(): String = "mahjong-guild-cache"

        override fun getCacheConfiguration(): RedisCacheConfiguration {
            return protoBufCacheConfiguration<MahjongGuildStat>(3.hours)
        }
    }
}