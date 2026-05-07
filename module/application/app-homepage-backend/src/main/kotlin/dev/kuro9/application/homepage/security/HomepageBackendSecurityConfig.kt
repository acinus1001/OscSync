package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.util.function.Supplier

@Configuration
class HomepageBackendSecurityConfig {

    @[Primary Bean]
    fun homepageFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(
                "/health",
                "/services/mahjong/**"
            )
            authorizeHttpRequests {
                authorize("/health", authenticated)
                authorize("/services/mahjong/**", withAuthority(MemberHomepageAuthority.Mahjong))
            }
        }

        return http.build()
    }

    private fun withAuthority(authority: MemberHomepageAuthority): AuthorizationManager<RequestAuthorizationContext> {
        return AuthorizationManager { auth: Supplier<Authentication>, _: RequestAuthorizationContext ->
            val auth = auth.get()
            val authorities = auth.authorities.map { it.authority }

            if (!auth.isAuthenticated) return@AuthorizationManager AuthorizationDecision(false)
            if (MemberRole.ROLE_ROOT.name in authorities) return@AuthorizationManager AuthorizationDecision(true)

            AuthorizationDecision(authority.toString() in authorities)
        }
    }
}