package dev.kuro9.application.osc.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication(scanBasePackages = ["dev.kuro9"])
class OscClientApplication

fun main(args: Array<String>) {
    runApplication<OscClientApplication>(*args)
}