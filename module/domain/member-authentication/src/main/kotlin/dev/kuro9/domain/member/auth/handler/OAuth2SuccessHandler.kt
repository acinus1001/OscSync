package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.config.CookieConfigProperties
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@[Component Order(Int.MAX_VALUE)]
class OAuth2SuccessHandler(
    private val tokenService: JwtTokenService,
    private val cookieProperties: CookieConfigProperties,
    private val authorizationSuccessHandlerList: List<AuthorizationSuccessHandler>
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userId = tokenService.getUserId(authentication)
        authorizationSuccessHandlerList.forEach { it.onSuccess(userId) }
        val tokenResponse = tokenService.makeTokenResponse(authentication)

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

        response.run {
            addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            sendRedirect(cookieProperties.redirectFrontUri)
        }
    }
}