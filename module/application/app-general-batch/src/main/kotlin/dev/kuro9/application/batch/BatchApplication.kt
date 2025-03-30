package dev.kuro9.application.batch

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = ["dev.kuro9"])
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}