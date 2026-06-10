package dev.kuro9.domain.member.auth.filter

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.jwt.JwtToken
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
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
        when (val accessToken = parseToken(request)) {
            null -> SecurityContextHolder.clearContext()
            else -> setAuthentication(accessToken)
        }
        filterChain.doFilter(request, response)
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

    private fun parseToken(request: HttpServletRequest): JwtToken? {
        val accessToken = request.cookies
            ?.firstOrNull { it.name == "accessToken" }
            ?.value

        return accessToken
            ?.let(::JwtToken)
            ?.takeIf(tokenService::isValid)
    }
}