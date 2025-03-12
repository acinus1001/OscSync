package dev.kuro9.domain.member.auth.config

import dev.kuro9.domain.member.auth.converter.JwtUserInfoConverter
import dev.kuro9.domain.member.auth.filter.TokenAuthFilter
import dev.kuro9.domain.member.auth.filter.TokenExceptionFilter
import dev.kuro9.domain.member.auth.handler.OAuth2SuccessHandler
import dev.kuro9.domain.member.auth.service.DiscordOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class OAuth2LoginSecurityConfig(
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        oAuth2UserService: DiscordOAuth2UserService,
        oAuth2SuccessHandler: OAuth2SuccessHandler,

        tokenExceptionFilter: TokenExceptionFilter,
        tokenAuthFilter: TokenAuthFilter,

        jwtTokenProperty: JwtTokenConfig.JwtProperty,

        jwtAuthConverter: JwtUserInfoConverter,
    ): SecurityFilterChain {
        http {
            csrf {
                ignoringRequestMatchers(
                    "/smartapp/webhook",
                )
            }
            cors {}
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/smartapp/webhook", permitAll)
                authorize("/login/success", permitAll)
                authorize("/login/success", permitAll)

                authorize(anyRequest, authenticated)
            }
            oauth2Login {
                userInfoEndpoint {
                    userService = oAuth2UserService
                    authenticationSuccessHandler = oAuth2SuccessHandler
                }

            }
//            oauth2ResourceServer {
//                jwt {
//                    jwtAuthenticationConverter = jwtAuthConverter
//                }
//            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(tokenAuthFilter)
            addFilterBefore<TokenAuthFilter>(tokenExceptionFilter)
        }

        return http.build()
    }
}