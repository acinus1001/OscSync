package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.repository.MemberAuthorities
import dev.kuro9.domain.member.auth.repository.MemberEntity
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
) : AuthorizationSuccessHandler {

    @Transactional
    override fun onSuccess(userId: Long) {
        val member = MemberEntity.findById(userId) ?: return
        val authoritiesToAdd = mutableSetOf<MemberHomepageAuthority>()

        runBlocking {

            // 마작
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Mahjong.toString() }) return@run

                val guildMemberInfo = try {
                    discordApiService.getGuildMemberInfo(guildId = 891599899420946463L, userId = userId)
                } catch (e: DiscordApiException.NotFound) {
                    info { "not on guild." }
                    return@run
                } catch (e: Exception) {
                    error(e) { "Failed to get guild member info. userId: $userId" }
                    return@run
                }

                // mahjong 관련 role 검사 등...
                authoritiesToAdd += MemberHomepageAuthority.Mahjong
            }

            // iot
            run {
                if (member.authorities.any { it.authority == MemberHomepageAuthority.Iot.toString() }) return@run

                // todo
            }
        }

        MemberAuthorities.batchInsert(authoritiesToAdd, ignore = true) {
            this[MemberAuthorities.authority] = it.toString()
            this[MemberAuthorities.createdAt] = LocalDateTime.now()
            this[MemberAuthorities.member] = userId
        }
    }
}