package dev.kuro9.domain.member.auth.config

import dev.kuro9.domain.member.auth.jwt.JwtSecretKey
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtTokenConfig {

    @Bean
    fun jwtTokenService() = JwtTokenService()

    @Bean
    fun jwtSecretKey() = JwtSecretKey("todo")
}