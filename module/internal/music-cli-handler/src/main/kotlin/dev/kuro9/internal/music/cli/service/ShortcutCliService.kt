package dev.kuro9.internal.music.cli.service

import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.exec.*
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@Service
class ShortcutCliService {
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)

    suspend fun playMusic(iTunesId: String) {
        require("^[0-9]$".toRegex().matchEntire(iTunesId) != null) { "Invalid iTunesId format: $iTunesId" }
        executeCommand("music_play", iTunesId)
    }

    suspend fun playlistAdd(iTunesId: String) {
        require("^[0-9]$".toRegex().matchEntire(iTunesId) != null) { "Invalid iTunesId format: $iTunesId" }
        executeCommand("music_playlist_add", iTunesId)
    }

    suspend fun startMusic() = executeCommand("music_start")

    suspend fun pauseMusic() = executeCommand("music_pause")

    suspend fun skipMusic() = executeCommand("music_skip")

    /**
     * @return iTunesId ( null if not playing )
     */
    suspend fun getNowPlaying(): String? = executeCommand("music_now_playing")
        .takeIf { it.isNotBlank() && it.all { c -> c.isDigit() } }

    suspend fun executeCommand(command: String, vararg args: String): String = withContext(dispatcher) {
        require("^[A-Za-z0-9_-]{1,50}$".toRegex().matchEntire(command) != null) { "Invalid command format: $command" }

        // args check
        for (arg in args) {
            require("^[0-9]{1,50}$".toRegex().matchEntire(arg) != null) { "Invalid argument format: $arg" }
        }

        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        val executor = DefaultExecutor.builder().get()
            .apply {
                watchdog = ExecuteWatchdog.builder().setTimeout(5_000.milliseconds.toJavaDuration()).get()
                streamHandler = PumpStreamHandler(outputStream, errorStream)
            }

        val commandLine = CommandLine("shortcuts")
            .addArgument("run")
            .addArgument(command)
            .addArgument("-i")
            .apply { args.forEach { this.addArgument(it) } }

        try {
            executor.execute(commandLine)
        } catch (e: ExecuteException) {
            error(e) { "shortcuts run error: ${e.message}" }
            errorStream.toString(Charsets.UTF_8).let {
                error { "stderr : $it" }
            }
        }

        return@withContext outputStream.toString(Charsets.UTF_8).trim()
    }
}