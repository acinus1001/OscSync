package dev.kuro9.application.discord

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableAsync
@EnableTransactionManagement
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = ["dev.kuro9"])
@SpringBootApplication(scanBasePackages = ["dev.kuro9"])
class DiscordBotApplication

fun main() {
    runApplication<DiscordBotApplication>()
}