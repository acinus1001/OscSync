package dev.kuro9.application.music.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@[RestController RequestMapping("/api/health")]
class HealthCheckController {

    @GetMapping(produces = ["text/plain"])
    fun healthCheck(): String {
        return "OK"
    }
}