package dev.kuro9.application.homepage.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
class HomepageBackendSecurityConfig {

    @[Primary Bean]
    fun homepageFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/health")
            authorizeHttpRequests {
                authorize("/health", authenticated)
            }
        }

        return http.build()
    }
}