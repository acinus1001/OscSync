package dev.kuro9.application.music.service

import dev.kuro9.application.music.dto.MusicInfo
import dev.kuro9.application.music.exception.MusicSearchException
import dev.kuro9.application.music.utils.toMusicInfo
import dev.kuro9.internal.itunes.service.ItunesApiService
import dev.kuro9.internal.music.cli.service.ShortcutCliService
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Service
@OptIn(ExperimentalAtomicApi::class)
class MusicStateService(
    private val shortcutCliService: ShortcutCliService,
    private val itunesApiService: ItunesApiService,
) {
    private val nowPlaying: AtomicReference<MusicInfo?> = AtomicReference(null)
    private val playQueue: ConcurrentLinkedQueue<MusicInfo> = ConcurrentLinkedQueue()
    private val isPaused: AtomicBoolean = AtomicBoolean(false)
    private val nowPlayingString: AtomicReference<String?> = AtomicReference(null)

    fun getNowPlaying(): MusicInfo? = nowPlaying.load()
    fun getPlayQueue(): List<MusicInfo> = playQueue.toList()

    @Throws(MusicSearchException::class)
    suspend fun addQueue(iTunesId: Long) {
        val info = itunesApiService.getItunesSongInfo(iTunesId)?.toMusicInfo()
            ?: throw MusicSearchException()

        if (nowPlaying.load() == null) {
            info { "play first music: ${info.artist} - ${info.title}" }
            nowPlaying.store(info)
            shortcutCliService.playMusic(iTunesId.toString())
            return
        }

        shortcutCliService.playlistAdd(iTunesId.toString())
        playQueue.add(info)
    }

    suspend fun skipMusic(): MusicInfo? {
        shortcutCliService.skipMusic()
        val nextMusic: MusicInfo? = playQueue.poll()

        if (nextMusic == null) {
            info { "play queue is empty. stop playing." }
            nowPlaying.store(null)
            return null
        }

        info { "play next music: ${nextMusic.artist} - ${nextMusic.title}" }
        nowPlaying.store(nextMusic)
        return nextMusic
    }

    suspend fun refreshNowPlaying(): MusicInfo? {
        val nowPlayingStringResult = shortcutCliService.getNowPlaying()
        val oldValue = nowPlayingString.exchange(nowPlayingStringResult)
        if (nowPlayingStringResult == null || nowPlayingStringResult == oldValue) return null

        val musicInfo = itunesApiService.searchMusic(nowPlayingStringResult)
            .firstOrNull()
            ?.toMusicInfo()
            ?: return null

        this.nowPlaying.store(musicInfo)
        return musicInfo
    }

    suspend fun pausePlaying() {
        shortcutCliService.pauseMusic()
        isPaused.store(true)
    }

    suspend fun resumePlaying() {
        shortcutCliService.startMusic()
        isPaused.store(false)
    }
}