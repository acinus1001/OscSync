package dev.kuro9.oscsync

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class OscSyncApplication

fun main(args: Array<String>) {
    runApplication<OscSyncApplication>(*args)
}
