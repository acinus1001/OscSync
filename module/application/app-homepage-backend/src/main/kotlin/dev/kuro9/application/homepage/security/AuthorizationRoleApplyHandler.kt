package dev.kuro9.application.homepage.security

import dev.kuro9.domain.discord.bot.guilds.config.DiscordBotProperty
import dev.kuro9.domain.discord.bot.guilds.service.DiscordBotGuildService
import dev.kuro9.domain.mahjong.core.service.MahjongScoreSettingService
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.repository.MemberAuthorities
import dev.kuro9.domain.member.auth.repository.MemberEntity
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import dev.kuro9.internal.discord.api.exception.DiscordApiException
import dev.kuro9.internal.discord.api.service.DiscordApiService
import dev.kuro9.multiplatform.common.date.util.now
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuthorizationRoleApplyHandler(
    private val discordApiService: DiscordApiService,
    private val botProperty: DiscordBotProperty,
    private val discordBotGuildService: DiscordBotGuildService,
    private val discordOAuthTokenService: DiscordOAuth2TokenManageService,
    private val mahjongScoreSettingService: MahjongScoreSettingService,
) : AuthorizationSuccessHandler {

    @Transactional
    override fun onSuccess(userId: Long) {
        val member = MemberEntity.findById(userId) ?: return
        val authoritiesToAdd = mutableSetOf<MemberHomepageAuthority>()

        runBlocking {

            // 길드별 마작
            run {
                val userGuildList = try {
                    val token = discordOAuthTokenService.getToken(userId) ?: return@run
                    discordApiService.getMyGuildList(userToken = token.accessToken)
                } catch (e: Exception) {
                    error(e) { "Failed to get guild list. userId: $userId" }
                    return@run
                }
                val botGuildList = discordBotGuildService.findGuildsByBotIdList(botProperty.id)

                val mutualIdList =
                    userGuildList.map { it.id.toLong() }.intersect(botGuildList.map { it.guildId }.toSet())
                        .filter { guildId ->
                            // save 시 score setting 무조건 생성되므로 세팅 존재하면 마작 기록한 것으로 간주할 수 있음.
                            mahjongScoreSettingService.hasScoreSettings(guildId)
                        }

                for (guildId in mutualIdList) {
                    authoritiesToAdd += MemberHomepageAuthority.MahjongGuild(guildId)
                }
            }

            // iot
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Iot.toString() }) return@run

                // todo
            }

            // VR
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Vr.toString() }) return@run

                // 기타 사용자
                if (member.id.value in listOf(893385640874500108L, 281001713815781378L)) {
                    authoritiesToAdd += MemberHomepageAuthority.Vr
                    return@run
                }

                val guildMemberInfo = try {
                    discordApiService.getGuildMemberInfo(guildId = 891599899420946463L, userId = userId)
                } catch (e: DiscordApiException.NotFound) {
                    info { "not on guild." }
                    return@run
                } catch (e: Exception) {
                    error(e) { "Failed to get guild member info. userId: $userId" }
                    return@run
                }

                authoritiesToAdd += MemberHomepageAuthority.Vr
            }
        }

        MemberAuthorities.batchInsert(authoritiesToAdd, ignore = true) {
            this[MemberAuthorities.authority] = it.toString()
            this[MemberAuthorities.createdAt] = LocalDateTime.now()
            this[MemberAuthorities.member] = userId
        }
    }
}