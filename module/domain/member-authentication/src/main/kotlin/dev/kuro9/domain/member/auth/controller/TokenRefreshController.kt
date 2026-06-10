package dev.kuro9.domain.member.auth.controller

import dev.kuro9.domain.member.auth.config.CookieConfigProperties
import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.jwt.JwtToken
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import io.github.harryjhin.slf4j.extension.info
import io.github.harryjhin.slf4j.extension.warn
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@RestController
@RequestMapping("/auth/refresh")
class TokenRefreshController(
    private val tokenService: JwtTokenService,
    private val discordTokenService: DiscordOAuth2TokenManageService,
    private val cookieProperties: CookieConfigProperties,
    private val authorizationSuccessHandlerList: List<AuthorizationSuccessHandler>,
) {

    @PostMapping
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Unit> {
        val refreshToken = request.cookies
            ?.firstOrNull { it.name == "refreshToken" }
            ?.value
            ?: return unauthorizedWithClearedCookies(response)

        return try {
            val userId = tokenService.getUserIdWithNoCheck(JwtToken(refreshToken))

            val tokenResponse = tokenService.refreshToken(refreshToken)
            info { "refresh token succeed." }

            discordTokenService.refreshToken(userId = userId)

            authorizationSuccessHandlerList.forEach { it.onSuccess(userId) }

            val accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken)
                .httpOnly(true)
                .secure(cookieProperties.secure)
                .domain(cookieProperties.domain)
                .sameSite("Lax")
                .path("/")
                .maxAge(30.minutes.toJavaDuration())
                .build()

            val refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.secure)
                .domain(cookieProperties.domain)
                .sameSite("Lax")
                .path("/")
                .maxAge(7.days.toJavaDuration())
                .build()

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            setAuthentication(JwtToken(tokenResponse.accessToken))
            ResponseEntity.noContent().build()
        } catch (e: JwtValidationException) {
            info(e) { "refresh token failed. clear cookies and context." }
            unauthorizedWithClearedCookies(response)
        } catch (e: Exception) {
            warn(e) { "refresh token failed. clear cookies and context." }
            unauthorizedWithClearedCookies(response)
        }
    }

    private fun unauthorizedWithClearedCookies(response: HttpServletResponse): ResponseEntity<Unit> {
        val accessTokenCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .domain(cookieProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(0.seconds.toJavaDuration())
            .build()

        val refreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .domain(cookieProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(0.seconds.toJavaDuration())
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())

        SecurityContextHolder.clearContext()

        return ResponseEntity.status(401).build()
    }

    private fun setAuthentication(accessToken: JwtToken) {
        val payload = tokenService.validateAndGetPayload(accessToken)
        val authorities = payload.scp.map(::SimpleGrantedAuthority)
        val user = DiscordUserDetail(
            id = payload.sub.toLong(),
            userName = payload.name,
            avatarUrl = payload.avatarUrl,
            role = payload.scp
                .filter { it.startsWith("ROLE_") }
                .map(MemberRole::valueOf)
                .single(),
            userAttr = emptyMap(),
            authorities = payload.scp
                .filter { !it.startsWith("ROLE_") }
        )
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, accessToken.token, authorities)
    }
}