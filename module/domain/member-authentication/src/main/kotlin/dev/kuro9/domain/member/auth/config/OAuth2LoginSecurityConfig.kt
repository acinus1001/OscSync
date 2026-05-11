package dev.kuro9.domain.member.auth.config

import dev.kuro9.domain.member.auth.filter.TokenAuthFilter
import dev.kuro9.domain.member.auth.interfaces.AuthorizeHttpRequestHandler
import dev.kuro9.domain.member.auth.service.DiscordOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Configuration
@EnableWebSecurity
class OAuth2LoginSecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        oAuth2UserService: DiscordOAuth2UserService,
        oAuth2SuccessHandler: AuthenticationSuccessHandler,
        tokenAuthFilter: TokenAuthFilter,
        cookieConfigProperties: CookieConfigProperties,
        authorizeHandlerList: List<AuthorizeHttpRequestHandler>
    ): SecurityFilterChain {
        http {
            csrf { disable() }
            cors {
                configurationSource = corsConfigurationSource()
            }
            httpBasic { disable() }
            formLogin { disable() }
            logout {
                logoutUrl = "/users/me/logout"
                logoutSuccessUrl = "/"
                logoutSuccessHandler = { _, response, _ ->
                    HttpStatusReturningLogoutSuccessHandler()
                    val expiredAccessTokenCookie = ResponseCookie.from("accessToken", "")
                        .httpOnly(true)
                        .secure(cookieConfigProperties.secure)
                        .domain(cookieConfigProperties.domain)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0.seconds.toJavaDuration())
                        .build()

                    val expiredRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(cookieConfigProperties.secure)
                        .domain(cookieConfigProperties.domain)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0.seconds.toJavaDuration())
                        .build()

                    response.addHeader(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
                    response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshTokenCookie.toString())
                    response.status = HttpStatus.OK.value()
                }
            }
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                authorize("/error", permitAll)
                authorize("/users/me", authenticated)

                for (handler in authorizeHandlerList) {
                    handler.applyCustomAuthorize(this)
                }

                authorize(anyRequest, denyAll)
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

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:8080",
                "http://localhost:8090",
                "https://kuro9.dev",
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}