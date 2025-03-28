package dev.kuro9.domain.error.handler.discord

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
class DiscordAopConfiguration