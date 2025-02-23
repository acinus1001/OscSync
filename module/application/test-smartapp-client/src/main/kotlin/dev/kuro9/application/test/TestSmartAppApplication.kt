package dev.kuro9.application.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication(scanBasePackages = ["dev.kuro9"])
class TestSmartAppApplication

fun main(args: Array<String>) {
    runApplication<TestSmartAppApplication>(*args)
}