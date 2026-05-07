package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.handler.OAuth2SuccessHandler
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.member.auth.repository.MemberAuthorities
import dev.kuro9.internal.discord.api.exception.DiscordApiException
import dev.kuro9.internal.discord.api.service.DiscordApiService
import dev.kuro9.multiplatform.common.date.util.now
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@[Primary Component]
class OAuth2SuccessRoleApplyHandler(
    private val origin: OAuth2SuccessHandler,
    private val discordApiService: DiscordApiService,
) : AuthenticationSuccessHandler {

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val userDetail = authentication.principal as DiscordUserDetail
        val userId = userDetail.id
        val authoritiesToAdd = mutableSetOf<MemberHomepageAuthority>()

        runBlocking {

            run {
                val guildMemberInfo = try {
                    discordApiService.getGuildMemberInfo(guildId = 891599899420946463L, userId = userId)
                } catch (e: DiscordApiException.NotFound) {
                    info { "not on guild." }
                    return@runBlocking
                } catch (e: Exception) {
                    error(e) { "Failed to get guild member info. userId: $userId" }
                    return@runBlocking
                }

                // mahjong 관련 role 검사 등...
                authoritiesToAdd += MemberHomepageAuthority.Mahjong
            }
        }

        val updatedAuth = if (authoritiesToAdd.isNotEmpty()) {
            val inserted = MemberAuthorities.batchInsert(authoritiesToAdd, ignore = true) {
                this[MemberAuthorities.authority] = it.toString()
                this[MemberAuthorities.createdAt] = LocalDateTime.now()
                this[MemberAuthorities.member] = userId
            }.count()

            if (inserted <= 0) authentication
            else {
                val updatedUserDetail = userDetail.copy(
                    authorities = (userDetail.authorities + authoritiesToAdd.map { it.toString() }).distinct()
                )

                UsernamePasswordAuthenticationToken(
                    updatedUserDetail,
                    authentication.credentials,
                    updatedUserDetail.getAuthorities(),
                ).apply {
                    details = authentication.details
                }
            }
        } else authentication

        origin.onAuthenticationSuccess(request, response, updatedAuth)
    }
}