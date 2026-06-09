package dev.kuro9.application.homepage.config

import dev.kuro9.domain.cache.CacheConfig.Companion.protoBufCacheConfiguration
import dev.kuro9.domain.cache.interfaces.CustomCacheConfigurer
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import kotlin.time.Duration.Companion.minutes

@Configuration
class HomePageCacheConfiguration {

    @Bean
    fun guildInfoCacheConfig(): CustomCacheConfigurer = object : CustomCacheConfigurer {
        override fun getCacheName(): String = "guild-info-cache"
        override fun getCacheConfiguration(): RedisCacheConfiguration {
            return protoBufCacheConfiguration<DiscordGuildInfo>(30.minutes)
        }
    }
}