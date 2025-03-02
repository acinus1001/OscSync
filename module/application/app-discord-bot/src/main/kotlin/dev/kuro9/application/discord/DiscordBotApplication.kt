package dev.kuro9.application.discord

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication(scanBasePackages = ["dev.kuro9"])
class DiscordBotApplication

fun main() {
    runApplication<DiscordBotApplication>()
}