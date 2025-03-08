package dev.kuro9.application.discord.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/api/test")
class TestApiController {

    @GetMapping
    fun test(): Map<String, String> {
        return mapOf(
            "value1" to "test",
            "hello" to "world"
        )
    }
}