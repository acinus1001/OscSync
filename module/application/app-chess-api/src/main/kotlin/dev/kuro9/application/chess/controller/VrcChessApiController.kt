package dev.kuro9.application.chess.controller

import dev.kuro9.internal.chess.engine.StockFishService
import io.github.harryjhin.slf4j.extension.info
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chess/eval")
class VrcChessApiController(
    private val chessService: StockFishService
) {

    @GetMapping
    suspend fun calculate(
        @RequestParam() move: String?,
    ): ResponseEntity<String> {
        val (bestMove, fen) = chessService.doMove(
            fen = "rnbqkbnr/ppp2ppp/3p4/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 3",
            move = move,
            afterUserMove = { move, nowFen ->
                info { "user move: $move, nowFen: $nowFen" }
            },
            afterEngineMove = { move, nowFen ->
                info { "engine move: $move, nowFen: $nowFen" }
            }
        )
        return ResponseEntity.ok(bestMove)
    }
}