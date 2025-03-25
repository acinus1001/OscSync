package dev.kuro9.domain.member.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("dev.kuro9.jwt")
data class JwtTokenConfigProperties @ConstructorBinding constructor(
    val key: String
)