package dev.kuro9.application.homepage.service

import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class GuildInfoCacheService {

    @Cacheable(value = ["guild-info-cache"], key = "#guildId", unless = "#result == null")
    fun getGuildInfoOrNull(guildId: Long): DiscordGuildInfo? {
        return null
    }

    @CachePut(value = ["guild-info-cache"], key = "#guildInfo.id")
    fun putGuildInfo(guildInfo: DiscordGuildInfo): DiscordGuildInfo {
        return guildInfo
    }
}