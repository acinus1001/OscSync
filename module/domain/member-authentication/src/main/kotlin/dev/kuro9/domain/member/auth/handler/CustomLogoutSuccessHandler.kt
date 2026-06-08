package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.config.CookieConfigProperties
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component
class CustomLogoutSuccessHandler(
    private val tokenService: JwtTokenService,
    private val cookieConfigProperties: CookieConfigProperties,
    private val discordTokenService: DiscordOAuth2TokenManageService,
) : LogoutSuccessHandler {
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?,
    ) {
        run {
            val userId = tokenService.getUserId(authentication ?: return@run)
            runBlocking {
                discordTokenService.revokeToken(userId)
            }
        }

        HttpStatusReturningLogoutSuccessHandler()
        val expiredAccessTokenCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(cookieConfigProperties.secure)
            .domain(cookieConfigProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(0.seconds.toJavaDuration())
            .build()

        val expiredRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieConfigProperties.secure)
            .domain(cookieConfigProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(0.seconds.toJavaDuration())
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshTokenCookie.toString())
        response.status = HttpStatus.OK.value()
    }
}