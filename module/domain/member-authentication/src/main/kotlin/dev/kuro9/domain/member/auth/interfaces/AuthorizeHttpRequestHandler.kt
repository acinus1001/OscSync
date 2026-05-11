package dev.kuro9.domain.member.auth.interfaces

import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl

interface AuthorizeHttpRequestHandler {
    fun applyCustomAuthorize(action: AuthorizeHttpRequestsDsl)
}