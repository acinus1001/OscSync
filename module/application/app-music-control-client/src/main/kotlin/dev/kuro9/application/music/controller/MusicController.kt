package dev.kuro9.application.music.controller

import dev.kuro9.application.music.dto.MusicInfo
import dev.kuro9.application.music.service.MusicStateService
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*

@[RestController RequestMapping("/api/music")]
class MusicController(
    private val musicStateService: MusicStateService,
) {

    @GetMapping("/now")
    fun getNowPlaying(): MusicInfo? {
        return musicStateService.getNowPlaying()
    }

    @GetMapping("/queue")
    fun getPlayQueue(): List<MusicInfo> {
        return musicStateService.getPlayQueue()
    }

    @PutMapping("/queue")
    suspend fun addQueue(@RequestParam("iTunesId") iTunesId: Long): MusicInfo {
        return musicStateService.addQueue(iTunesId)
    }

    @PostMapping("/now/skip")
    suspend fun skipMusic() {
        musicStateService.skipMusic()
    }

    @PostMapping("/now/pause")
    suspend fun pauseMusic() {
        musicStateService.pausePlaying()
    }

    @PostMapping("/now/resume")
    suspend fun resumeMusic() {
        musicStateService.resumePlaying()
    }

    @Scheduled(fixedRate = 10_000L) // 10초마다 한번 리프레시
    fun refreshNowPlaying() {
        info { "[Scheduled] refresh now playing" }
        runBlocking { musicStateService.refreshNowPlaying() }
    }
}