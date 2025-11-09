package dev.kuro9.internal.chess.engine

import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.*
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@Service
class StockFishService(private val resourceLoader: ResourceLoader) {

    private val stockfishPath: String by lazy { extractStockfishBinary() }
    private val callbackLaunchContext = Dispatchers.IO + CoroutineName("StockfishCallback")

    suspend fun doMove(
        fen: String,
        move: String? = null,
        afterUserMove: (suspend (move: String, prevFen: String, nowFen: String) -> Unit)? = null,
        afterEngineMove: suspend (move: String, prevFen: String, nowFen: String) -> Unit = { _, _, _ -> },
        callbackFailureHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, e -> error(e) { "exception in callback" } },
        movetimeMs: Int = 500,
        elo: Int = 200
    ): ChessMoveDto {
        val proc = ProcessBuilder(stockfishPath)
            .redirectErrorStream(true)
            .start()

        val writer = proc.outputStream.bufferedWriter()
        val reader = proc.inputStream.bufferedReader()

        val jobs = mutableListOf<Job>()

        // UCI 초기화
        writer.write("uci\n")
        writer.flush()
        waitForOutput(reader, "uciok")

        val skill = mapEloToSkill(elo)
        writer.write("setoption name Skill Level value $skill\n")
        writer.flush()

        writer.write("isready\n")
        writer.flush()
        waitForOutput(reader, "readyok")

        val afterMoveFen = when (move) {
            null -> {
                writer.write("position fen $fen\n")
                writer.flush()
                fen
            }

            else -> {
                writer.write("position fen $fen moves $move\n")
                writer.flush()


                writer.write("d\n")
                writer.flush()

                var fenLine: String by Delegates.notNull()
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("Fen: ")) {
                        fenLine = line.removePrefix("Fen: ").trim()
                        break
                    }
                    if (line.startsWith("bestmove")) break // 혹시 끊기면
                }

                info { "fenLine: $fenLine" }

                CoroutineScope(callbackLaunchContext).launch(callbackFailureHandler) {
                    afterUserMove?.invoke(move, fen, fenLine)
                }.let { jobs.add(it) }


                fenLine
            }
        }

        writer.write("go movetime $movetimeMs\n")
        writer.flush()

        var bestMove: String by Delegates.notNull()
        val deadline = System.currentTimeMillis() + movetimeMs + 2000

        while (System.currentTimeMillis() < deadline) {
            val line = reader.readLine() ?: break
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1]
                break
            }
        }


        // bestmove 적용해서 fen 출력
        writer.write("position fen $afterMoveFen moves $bestMove\n")
        writer.flush()


        writer.write("d\n")
        writer.flush()

        var fenLine: String by Delegates.notNull()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith("Fen: ")) {
                fenLine = line.removePrefix("Fen: ").trim()
                break
            }
            if (line.startsWith("bestmove")) break // 혹시 끊기면
        }

        writer.write("quit\n")
        writer.flush()
        proc.waitFor(1, TimeUnit.SECONDS)

        CoroutineScope(callbackLaunchContext).launch(callbackFailureHandler) {
            afterEngineMove(bestMove, afterMoveFen, fenLine)
        }.let { jobs.add(it) }

        jobs.joinAll()

        return ChessMoveDto(
            bestMove = bestMove,
            fen = fenLine,
        )
    }

    private fun waitForOutput(reader: java.io.BufferedReader, keyword: String, timeoutMs: Long = 2000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val line = reader.readLine() ?: continue
            if (line.startsWith(keyword)) return
        }
    }

    private fun extractStockfishBinary(): String {
        val (os, arch) = detectPlatform()
        val resourcePath = "engines/${os}-${arch}/" +
                if (os == "windows") "stockfish.exe" else "stockfish"

        resourceLoader.getResource("classpath:${resourcePath}")

        val inputStream: InputStream = resourceLoader.getResource("classpath:${resourcePath}").inputStream
            ?: throw IllegalStateException("Stockfish binary not found in resources: $resourcePath")

        // 임시 파일 생성 (OS별로 실행 가능하도록)
        val tempFile = Files.createTempFile("stockfish", if (os == "windows") ".exe" else "").toFile()
        tempFile.deleteOnExit()
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        if (!os.startsWith("win")) {
            tempFile.setExecutable(true)
        }

        return tempFile.absolutePath
    }

    private fun detectPlatform(): Pair<String, String> {
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()

        val osType = when {
            os.contains("mac") -> "mac"
            os.contains("win") -> "windows"
            os.contains("nux") || os.contains("nix") -> "linux"
            else -> "unknown"
        }

        val archType = when {
            arch.contains("aarch64") || arch.contains("arm") -> "arm"
            arch.contains("x86_64") || arch.contains("amd64") -> "x64"
            else -> "unknown"
        }

        return Pair(osType, archType)
    }

    private fun mapEloToSkill(elo: Int): Int {
        return when {
            elo < 400 -> 1
            elo <= 500 -> 2
            elo <= 800 -> 3
            elo <= 1100 -> 4
            elo <= 1500 -> 5
            elo <= 1900 -> 6
            else -> 7
        }
    }
}