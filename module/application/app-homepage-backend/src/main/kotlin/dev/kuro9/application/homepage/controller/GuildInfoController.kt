package dev.kuro9.application.homepage.controller

import dev.kuro9.application.homepage.security.MemberHomepageAuthority
import dev.kuro9.domain.discord.bot.guilds.config.DiscordBotProperty
import dev.kuro9.domain.discord.bot.guilds.service.DiscordBotGuildService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import dev.kuro9.internal.discord.api.service.DiscordApiService
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import io.github.harryjhin.slf4j.extension.info
import io.github.harryjhin.slf4j.extension.warn
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/common/info/guilds")
class GuildInfoController(
    private val discordApiService: DiscordApiService,
    private val discordOAuthTokenService: DiscordOAuth2TokenManageService,
    private val discordBotGuildService: DiscordBotGuildService,
    private val discordBotProperty: DiscordBotProperty,
) {

    @PostMapping("/bulk")
    fun getBulkGuildInfo(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @RequestBody guildIdList: List<Long>
    ): List<DiscordGuildInfo> {
        // check authorities
        val guildIdListOfHasPermission = guildIdList.filter { guildId ->
            user.hasAllPermissionOf(MemberHomepageAuthority.MahjongGuild(guildId).toString())
        }

        info { "getBulkGuildInfo: 요청=$guildIdList 필터링된 요청=$guildIdListOfHasPermission" }

        if (guildIdListOfHasPermission.isEmpty()) {
            return emptyList()
        }

        // 1차로 유저 token 이용해 discord에서 guild 가져온다.
        val guildInfo = runBlocking {
            try {
                val token = discordOAuthTokenService.getToken(user.id) ?: return@runBlocking null
                val myGuilds = discordApiService.getMyGuildList(userToken = token.accessToken)
                return@runBlocking myGuilds.filter { guild -> guild.idLong in guildIdListOfHasPermission }
            } catch (e: Exception) {
                warn(e) { "getBulkGuildInfo: 에러 발생: ${e.message}" }
                null
            }
        }

        if (guildInfo != null) {
            return guildInfo.map { guild ->
                DiscordGuildInfo(
                    id = guild.idLong,
                    name = guild.name,
                    iconUrl = guild.iconUrl
                )
            }
        }

        // 모종의 이유로 실패 시 db에 저장된 데이터를 꺼내온다.
        val result = discordBotGuildService.findGuildsByBotIdAndGuildIdList(
            discordBotProperty.id,
            guildIdList = guildIdListOfHasPermission
        )

        return result.map { guild ->
            DiscordGuildInfo(
                id = guild.guildId,
                name = guild.guildName,
                iconUrl = guild.guildIconUrl
            )
        }
    }
}