package dev.kuro9.domain.member.auth.filter

import dev.kuro9.common.logger.infoLog
import dev.kuro9.domain.member.auth.jwt.JwtToken
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuthFilter(
    private val tokenService: JwtTokenService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = parseToken(request)

        when (accessToken) {
            null -> {
                infoLog("accessToken is null")
                // refreshToken 사용가능한 경우 토큰 재발급 로직을 여기에 작성

                // 토큰 발급 실패한 경우
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is not valid.")
            }

            else -> {
                // set authentication
                val payload = tokenService.validateAndGetPayload(accessToken)
                val authorities = payload.scp.map(::SimpleGrantedAuthority)
                val user = User(payload.sub, "", authorities)
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(user, accessToken.token, authorities)
            }
        }
    }

    private fun parseToken(request: HttpServletRequest): JwtToken? {
        val accessToken = request.cookies
            ?.firstOrNull { it.name == "accessToken" }
            ?.value
            ?: request
                .getHeader(HttpHeaders.AUTHORIZATION)
                ?.removePrefix("Bearer ")

        return accessToken
            ?.let(::JwtToken)
            ?.takeIf(tokenService::isValid)
    }
}