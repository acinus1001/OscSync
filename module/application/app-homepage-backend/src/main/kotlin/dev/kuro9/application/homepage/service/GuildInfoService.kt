package dev.kuro9.application.homepage.service

import dev.kuro9.domain.discord.bot.guilds.config.DiscordBotProperty
import dev.kuro9.domain.discord.bot.guilds.service.DiscordBotGuildService
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import dev.kuro9.internal.discord.api.service.DiscordApiService
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import dev.kuro9.multiplatform.common.types.discord.api.DiscordGuild
import io.github.harryjhin.slf4j.extension.info
import io.github.harryjhin.slf4j.extension.warn
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class GuildInfoService(
    private val discordApiService: DiscordApiService,
    private val discordOAuthTokenService: DiscordOAuth2TokenManageService,
    private val discordBotGuildService: DiscordBotGuildService,
    private val discordBotProperty: DiscordBotProperty,
    private val cacheService: GuildInfoCacheService,
) {

    fun getGuildInfo(userId: Long, vararg guildIds: Long): List<DiscordGuildInfo> {
        if (guildIds.isEmpty()) return emptyList()

        // 캐시 조회
        val guildInfoInCache = guildIds.toList().mapNotNull { cacheService.getGuildInfoOrNull(it) }
        val guildInfoNotInCache = guildIds.toSet() - guildInfoInCache.map { it.id }.toSet()

        if (guildInfoNotInCache.isEmpty()) {
            info { "all guild info cache hit." }
            return guildInfoInCache
        }

        info { "guild info cache miss count : ${guildInfoNotInCache.size}. proceed with oauth token api." }

        // 1차로 유저 token 이용해 discord에서 guild 가져온다.
        val guildInfo: List<DiscordGuild>? = runBlocking {
            try {
                val token = discordOAuthTokenService.getToken(userId) ?: return@runBlocking null
                val myGuilds = discordApiService.getMyGuildList(userToken = token.accessToken)
                return@runBlocking myGuilds.filter { guild -> guild.idLong in guildInfoNotInCache }
            } catch (e: Exception) {
                warn(e) { "getBulkGuildInfo: 에러 발생: ${e.message}" }
                null
            }
        }

        if (guildInfo != null) {
            val resultInfoList = guildInfo.map { guild ->
                DiscordGuildInfo(
                    id = guild.idLong,
                    name = guild.name,
                    iconUrl = guild.iconUrl
                ).also { cacheService.putGuildInfo(it) }
            }

            return guildInfoInCache + resultInfoList
        }

        // 모종의 이유로 실패 시 db에 저장된 데이터를 꺼내온다.
        val result = discordBotGuildService.findGuildsByBotIdAndGuildIdList(
            discordBotProperty.id,
            guildIdList = guildInfoNotInCache.toList()
        )

        return result.map { guild ->
            DiscordGuildInfo(
                id = guild.guildId,
                name = guild.guildName,
                iconUrl = guild.guildIconUrl
            ).also { cacheService.putGuildInfo(it) }
        } + guildInfoInCache
    }
}