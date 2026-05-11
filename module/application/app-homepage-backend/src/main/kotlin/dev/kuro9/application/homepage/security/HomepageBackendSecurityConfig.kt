package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.interfaces.AuthorizeHttpRequestHandler
import io.github.harryjhin.slf4j.extension.info
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.annotation.web.AuthorizeHttpRequestsDsl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.web.cors.CorsConfigurationSource
import java.util.function.Supplier

@Configuration
class HomepageBackendSecurityConfig {

    fun homepageFilterChain(
        http: HttpSecurity,
        @Qualifier("corsConfigurationSource") cors: CorsConfigurationSource
    ): SecurityFilterChain {
        http {
            securityMatcher(
                "/health",
                "/services/mahjong/**",
                "/services/iot/**",

                "/webhook/smartapp",
            )
            csrf { disable() }
            cors { configurationSource = cors }
            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                authorize("/health", permitAll)
                authorize("/webhook/smartapp", permitAll)
                authorize("/services/iot/noti/subscribe", permitAll)
                authorize("/services/mahjong/**", withAuthority(MemberHomepageAuthority.Mahjong))
                authorize("/services/iot/**", withAuthority(MemberHomepageAuthority.Iot))
            }
        }

        return http.build()
    }

    @Bean
    fun homepageAuthorizeRequests(): AuthorizeHttpRequestHandler = object : AuthorizeHttpRequestHandler {
        override fun applyCustomAuthorize(action: AuthorizeHttpRequestsDsl) {
            action.apply {
                authorize("/health", permitAll)
                authorize("/webhook/smartapp", permitAll)
                authorize("/services/iot/noti/subscribe", permitAll)
                authorize("/services/mahjong/**", withAuthority(MemberHomepageAuthority.Mahjong))
                authorize("/services/iot/**", withAuthority(MemberHomepageAuthority.Iot))
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

            if (!auth.isAuthenticated) return@AuthorizationManager AuthorizationDecision(false)
            if (MemberRole.ROLE_ROOT.name in authorities) return@AuthorizationManager AuthorizationDecision(true)

            AuthorizationDecision(authority.toString() in authorities)
        }
    }
}