package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

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

        UriComponentsBuilder.fromUriString("/auth/success/todo")
            .queryParam("token", accessToken.token)
            .build()
            .toUriString()
            .let(response::sendRedirect)
    }
}