package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Component
class OAuth2SuccessHandler(
    private val tokenService: JwtTokenService,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val accessToken = tokenService.makeToken(authentication)

        val accessTokenCookie = ResponseCookie.from("accessToken", accessToken.token)
            .httpOnly(true)
            .secure(true)
            .domain("localhost")
            .sameSite("none")
            .path("/")
            .maxAge(30.minutes.toJavaDuration())
            .build()

        response.run {
            addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            sendRedirect("/")
        }
    }
}