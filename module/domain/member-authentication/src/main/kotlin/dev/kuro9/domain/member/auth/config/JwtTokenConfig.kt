package dev.kuro9.domain.member.auth.config

import dev.kuro9.domain.member.auth.jwt.JwtSecretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtTokenConfig {

    @Bean
    fun jwtSecretKey(
        @Value("\${dev.kuro9.jwt.key}") secretKey: String
    ) = JwtSecretKey(secretKey)

    @Bean
    fun jwtProperty(
        @Value("\${dev.kuro9.jwt.redirect-url}") redirectUrl: String
    ) = JwtProperty(redirectUrl)

    data class JwtProperty(
        val redirectUrl: String,
    )
}