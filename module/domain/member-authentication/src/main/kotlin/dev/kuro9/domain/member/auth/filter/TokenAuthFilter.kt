package dev.kuro9.domain.member.auth.filter

import dev.kuro9.domain.member.auth.config.CookieConfigProperties
import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.jwt.JwtToken
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import io.github.harryjhin.slf4j.extension.info
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component
class TokenAuthFilter(
    private val tokenService: JwtTokenService,
    private val cookieProperties: CookieConfigProperties,
    private val authorizationSuccessHandlerList: List<AuthorizationSuccessHandler>
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        when (val accessToken = parseToken(request)) {
            null -> run {
                val refreshToken = request.cookies?.firstOrNull { it.name == "refreshToken" }?.value
                if (refreshToken == null) {
                    SecurityContextHolder.clearContext()
                    return@run
                }

                if (tokenService.isRefreshable(refreshToken)) {
                    val userId = tokenService.getUserIdWithNoCheck(refreshToken.let(::JwtToken))
                    authorizationSuccessHandlerList.forEach { it.onSuccess(userId) }
                }
                val tokenResponse = try {
                    tokenService.refreshToken(refreshToken)
                } catch (e: JwtValidationException) {
                    info(e) { "not valid refresh token. clear cookies and context. message=${e.message}" }

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
                    return@run
                }

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
            }

            else -> {
                setAuthentication(accessToken)
            }
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