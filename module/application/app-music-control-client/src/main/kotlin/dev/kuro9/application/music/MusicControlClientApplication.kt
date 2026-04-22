package dev.kuro9.application.music

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@ComponentScan(basePackages = ["dev.kuro9"])
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
class MusicControlClientApplication

fun main(args: Array<String>) {
    runApplication<MusicControlClientApplication>(*args) {}
}