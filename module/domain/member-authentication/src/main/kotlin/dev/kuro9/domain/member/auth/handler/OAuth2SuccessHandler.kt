package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.jwt.JwtPayloadV1
import dev.kuro9.domain.member.auth.jwt.JwtSecretKey
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.Clock
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import kotlin.time.Duration.Companion.minutes

@Component
class OAuth2SuccessHandler(
    private val tokenService: JwtTokenService,
    private val secretKey: JwtSecretKey
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val authorities = authentication.authorities.map { it.authority }
        val payload = JwtPayloadV1(
            sub = authentication.name,
            iat = Clock.System.now(),
            exp = Clock.System.now() + 30.minutes,
        )
        val accessToken = tokenService.makeToken(payload, secretKey.value)

        UriComponentsBuilder.fromUriString("/auth/success/todo")
            .queryParam("token", accessToken.token)
            .build()
            .toUriString()
            .let(response::sendRedirect)
    }
}