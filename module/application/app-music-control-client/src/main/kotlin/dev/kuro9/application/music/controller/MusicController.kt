package dev.kuro9.application.music.controller

import dev.kuro9.application.music.service.MusicStateService
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@[RestController RequestMapping("/api/music")]
class MusicController(
    private val musicStateService: MusicStateService,
) {

    fun getNowPlaying() {

    }

    fun getPlayQueue() {
    }

    fun addQueue() {
    }

    fun skipMusic() {
    }

    fun pauseMusic() {
    }

    fun resumeMusic() {
    }

    @Scheduled(fixedRate = 10_000L) // 10초마다 한번 리프레시
    fun refreshNowPlaying() {
        runBlocking { musicStateService.refreshNowPlaying() }
    }
}