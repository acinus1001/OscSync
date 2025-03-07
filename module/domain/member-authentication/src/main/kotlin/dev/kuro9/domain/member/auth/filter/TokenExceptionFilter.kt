package dev.kuro9.domain.member.auth.filter

import dev.kuro9.common.logger.errorLog
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenExceptionFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: JwtValidationException) {
            errorLog("jwt validate failed", e)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired or malformed.")
        }
    }
}