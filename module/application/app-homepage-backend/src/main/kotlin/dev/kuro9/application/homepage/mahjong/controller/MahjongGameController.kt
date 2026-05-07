package dev.kuro9.application.homepage.mahjong.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/services/mahjong/games")
class MahjongGameController {

    @GetMapping
    fun getAllGames() {
        TODO()
    }
}