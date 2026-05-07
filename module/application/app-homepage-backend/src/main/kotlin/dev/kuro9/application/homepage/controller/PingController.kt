package dev.kuro9.application.homepage.controller

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/health")
class PingController {

    @GetMapping
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok(LocalDateTime.now().toString())
    }
}