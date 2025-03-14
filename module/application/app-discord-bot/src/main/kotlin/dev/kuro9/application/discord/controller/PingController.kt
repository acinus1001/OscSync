package dev.kuro9.application.discord.controller

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.intLiteral
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ping")
class PingController {

    @Transactional
    @GetMapping
    fun ping(): ResponseEntity<Nothing> {
        Table.Dual.select(intLiteral(1))
            .first()[intLiteral(1)]

        return ResponseEntity.ok().build<Nothing>()
    }
}