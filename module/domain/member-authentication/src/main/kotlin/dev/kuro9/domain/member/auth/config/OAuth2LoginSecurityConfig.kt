package dev.kuro9.domain.member.auth.config

import dev.kuro9.domain.member.auth.filter.TokenAuthFilter
import dev.kuro9.domain.member.auth.handler.OAuth2SuccessHandler
import dev.kuro9.domain.member.auth.service.DiscordOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler

@Configuration
@EnableWebSecurity
class OAuth2LoginSecurityConfig(
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        oAuth2UserService: DiscordOAuth2UserService,
        oAuth2SuccessHandler: OAuth2SuccessHandler,
        tokenAuthFilter: TokenAuthFilter,
    ): SecurityFilterChain {
        http {
            csrf { disable() }
            cors { }
            httpBasic { disable() }
            formLogin { disable() }
            logout {
                logoutUrl = "/api/user/logout"
                logoutSuccessUrl = "/"
                logoutSuccessHandler = HttpStatusReturningLogoutSuccessHandler()
                deleteCookies("accessToken")
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {

                authorize("/", permitAll)
                authorize("/*.js", permitAll)
                authorize("/*.html", permitAll)
                authorize("/*.wasm", permitAll)
                authorize("/static/**", permitAll)
                authorize("/error", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/smartapp/webhook", permitAll)

                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                userInfoEndpoint {
                    userService = oAuth2UserService
                    authenticationSuccessHandler = oAuth2SuccessHandler
                }

            }
            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(tokenAuthFilter)
            // addFilterBefore<TokenAuthFilter>(tokenExceptionFilter)
        }

        return http.build()
    }
}