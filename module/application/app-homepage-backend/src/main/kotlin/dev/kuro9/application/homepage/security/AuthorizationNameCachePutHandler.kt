package dev.kuro9.application.homepage.security

import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.repository.MemberEntity
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 로그인 성공 시 닉네임 캐시 저장
 */
@Component
class AuthorizationNameCachePutHandler(
    private val discordSearchService: DiscordSearchService
) : AuthorizationSuccessHandler {

    @Transactional(readOnly = true)
    override fun onSuccess(userId: Long) {
        val member = MemberEntity.findById(userId) ?: return

        discordSearchService.updateDiscordName(userId, member.name)
    }
}