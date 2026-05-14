package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.interfaces.AuthorizeHttpRequestHandler
import io.github.harryjhin.slf4j.extension.info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import java.util.function.Supplier

@Configuration
class HomepageBackendSecurityConfig {

    @Bean
    fun homepageAuthorizeRequests(): AuthorizeHttpRequestHandler = object : AuthorizeHttpRequestHandler {
        override fun applyCustomAuthorize(action: AuthorizeHttpRequestsDsl) {
            action.apply {
                authorize("/health", permitAll)
                authorize("/webhook/smartapp", permitAll)
                authorize("/services/iot/noti/subscribe", permitAll)
                authorize("/services/mahjong/**", withAuthority(MemberHomepageAuthority.Mahjong))
                authorize("/services/iot/**", withAuthority(MemberHomepageAuthority.Iot))

                authorize("/resources/strings/*", authenticated)
                authorize("/resources/images/*", authenticated)
                authorize("/resources/admin/**", hasRole("ROOT"))
            }
        }
    }

    /**
     * 주어진 Authority 및 ROLE_ROOT 에 한해 접근 권한 허용
     */
    private fun withAuthority(authority: MemberHomepageAuthority): AuthorizationManager<RequestAuthorizationContext> {
        return AuthorizationManager { auth: Supplier<Authentication>, _: RequestAuthorizationContext ->
            val auth = auth.get()
            val authorities = auth.authorities.map { it.authority }

            info { "auth: $auth, authorities: $authorities" }

            if (auth is AnonymousAuthenticationToken || !auth.isAuthenticated)
                return@AuthorizationManager AuthorizationDecision(false)
            if (MemberRole.ROLE_ROOT.name in authorities) return@AuthorizationManager AuthorizationDecision(true)

            AuthorizationDecision(authority.toString() in authorities)
        }
    }
}