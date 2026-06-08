package dev.kuro9.application.homepage.mahjong.controller

import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/services/mahjong/games")
class MahjongGameController(
    private val statService: MahjongStatService,
) {

    @GetMapping
    fun getAllGames() {
        TODO()
    }
}