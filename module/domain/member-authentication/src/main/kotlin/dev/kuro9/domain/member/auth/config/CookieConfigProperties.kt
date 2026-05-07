package dev.kuro9.domain.member.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("dev.kuro9.cookie")
data class CookieConfigProperties @ConstructorBinding constructor(
    val redirectFrontUri: String,
    val domain: String,
    val secure: Boolean,
)