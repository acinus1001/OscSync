package dev.kuro9.application.music

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
class MusicControlClientApplication

fun main(args: Array<String>) {
    runApplication<MusicControlClientApplication>(*args) {}
}