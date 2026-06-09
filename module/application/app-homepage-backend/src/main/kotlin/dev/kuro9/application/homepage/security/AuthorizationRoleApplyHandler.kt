package dev.kuro9.application.homepage.security

import dev.kuro9.application.homepage.service.GuildInfoCacheService
import dev.kuro9.domain.discord.bot.guilds.config.DiscordBotProperty
import dev.kuro9.domain.discord.bot.guilds.service.DiscordBotGuildService
import dev.kuro9.domain.mahjong.core.service.MahjongScoreSettingService
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.repository.MemberAuthorities
import dev.kuro9.domain.member.auth.repository.MemberEntity
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import dev.kuro9.internal.discord.api.service.DiscordApiService
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AuthorizationRoleApplyHandler(
    private val discordApiService: DiscordApiService,
    private val botProperty: DiscordBotProperty,
    private val discordBotGuildService: DiscordBotGuildService,
    private val discordOAuthTokenService: DiscordOAuth2TokenManageService,
    private val mahjongScoreSettingService: MahjongScoreSettingService,
    private val guildInfoCacheService: GuildInfoCacheService,
) : AuthorizationSuccessHandler {

    @Transactional
    override fun onSuccess(userId: Long) {
        val member = MemberEntity.findById(userId) ?: return
        val authoritiesToAdd = mutableSetOf<MemberHomepageAuthority>()
        val authoritiesToRemove = mutableSetOf<MemberHomepageAuthority>()

        runBlocking {
            val userGuildList = run {
                try {
                    val token = discordOAuthTokenService.getToken(userId) ?: return@run null
                    discordApiService.getMyGuildList(userToken = token.accessToken)
                } catch (e: Exception) {
                    error(e) { "Failed to get guild list. userId: $userId" }
                    null
                }
            }


            // 길드별 마작
            run {
                if (userGuildList == null) {
                    info { "userGuildList is null. userId: $userId" }
                    return@run
                }

                val botGuildList = discordBotGuildService.findGuildsByBotIdList(botProperty.id)

                val mutualIdList =
                    userGuildList.map { it.id.toLong() }.intersect(botGuildList.map { it.guildId }.toSet())
                        .filter { guildId ->
                            // save 시 score setting 무조건 생성되므로 세팅 존재하면 마작 기록한 것으로 간주할 수 있음.
                            mahjongScoreSettingService.hasScoreSettings(guildId)
                        }

                val granted = mutualIdList.map { MemberHomepageAuthority.MahjongGuild(it) }.toSet()

                authoritiesToAdd += granted

                // 미리 캐시에 저장
                for (guildId in mutualIdList) {
                    val guildInfo = userGuildList.firstOrNull { it.id == guildId.toString() } ?: continue
                    DiscordGuildInfo(
                        id = guildInfo.idLong,
                        name = guildInfo.name,
                        iconUrl = guildInfo.iconUrl
                    ).let { guildInfoCacheService.putGuildInfo(it) }
                }

                // 길드별 마작 권한의 경우 길드 탈퇴 시 revoke
                authoritiesToRemove += member.authorities.asSequence().map { it.authority }
                    .filter { authority: String -> authority.startsWith("AUTHORITY_${MemberHomepageAuthority.MahjongGuild.getPrefix()}") }
                    .mapNotNull { MemberHomepageAuthority.MahjongGuild.parseOrNull(it) }
                    .toSet()
                    .minus(granted)
            }

            // iot
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Iot.toString() }) return@run

                // todo
            }

            // VR
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Vr.toString() }) return@run
                if (userGuildList == null) return@run

                // 기타 사용자
                if (member.id.value in listOf(893385640874500108L, 281001713815781378L)) {
                    authoritiesToAdd += MemberHomepageAuthority.Vr
                    return@run
                }

                if (userGuildList.firstOrNull { it.id == "891599899420946463" } == null) return@run

                authoritiesToAdd += MemberHomepageAuthority.Vr
            }
        }

        info { "apply authorities: $authoritiesToAdd, remove authorities: $authoritiesToRemove" }

        MemberAuthorities.deleteWhere {
            (MemberAuthorities.member eq userId)
                .and(MemberAuthorities.authority inList authoritiesToRemove.map { it.toString() })
        }

        MemberAuthorities.batchInsert(authoritiesToAdd, ignore = true) {
            this[MemberAuthorities.authority] = it.toString()
            this[MemberAuthorities.createdAt] = LocalDateTime.now()
            this[MemberAuthorities.member] = userId
        }
    }
}