package dev.kuro9.application.chess.service

import dev.kuro9.application.chess.exception.GameNotExistException
import dev.kuro9.domain.chess.integration.vrc.dto.ChessPlayerInfo
import dev.kuro9.domain.chess.integration.vrc.enums.ChessTurn
import dev.kuro9.domain.chess.integration.vrc.service.VrcChessStateService
import dev.kuro9.internal.chess.engine.StockFishService
import dev.kuro9.multiplatform.common.chess.util.getMoveSan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VrcChessService(
    private val chessEngineService: StockFishService,
    private val chessStateService: VrcChessStateService,
) {

    @Transactional(rollbackFor = [Exception::class])
    suspend fun makeBotGame(
        userIp: String,
        userTurn: ChessTurn,
        botELO: Int,
    ): String? {
        val (white, black) = when (userTurn) {
            ChessTurn.WHITE -> ChessPlayerInfo.User.fromIp(userIp) to ChessPlayerInfo.Bot
            ChessTurn.BLACK -> ChessPlayerInfo.Bot to ChessPlayerInfo.User.fromIp(userIp)
        }

        val newGame = chessStateService.startNewGame(
            white = white,
            black = black,
            botELO = botELO,
        )

        // 플레이어가 흑이면 봇 move 실행 후 저장, 리턴
        if (userTurn == ChessTurn.WHITE) return null

        return chessEngineService.doMove(
            fen = newGame.fen,
            move = null,
            afterUserMove = null,
            afterEngineMove = { move, prevFen, nowFen ->
                chessStateService.updateMove(
                    white = ChessPlayerInfo.of(newGame.whiteUserIpHash),
                    black = ChessPlayerInfo.of(newGame.blackUserIpHash),
                    nowTurn = ChessTurn.WHITE,
                    newFen = nowFen,
                    move = move,
                    pgn = getMoveSan(prevFen, nowFen),
                )
            },
            movetimeMs = 500,
            elo = newGame.botELO!!,
        ).bestMove
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun doMove(
        userIp: String,
        userMove: String,
    ): String {
        val nowGame = chessStateService.getBotGame(user = ChessPlayerInfo.User.fromIp(userIp))
            ?: throw GameNotExistException()

        val userTurn = if (ChessPlayerInfo.User.fromIp(userIp).ipHash == nowGame.whiteUserIpHash) {
            ChessTurn.WHITE
        } else ChessTurn.BLACK

        return chessEngineService.doMove(
            fen = nowGame.fen,
            move = userMove,
            afterUserMove = { move, prevFen, nowFen ->
                chessStateService.updateMove(
                    white = ChessPlayerInfo.of(nowGame.whiteUserIpHash),
                    black = ChessPlayerInfo.of(nowGame.blackUserIpHash),
                    nowTurn = userTurn,
                    newFen = nowFen,
                    move = move,
                    pgn = getMoveSan(prevFen, nowFen),
                )
            },
            afterEngineMove = { move, prevFen, nowFen ->
                chessStateService.updateMove(
                    white = ChessPlayerInfo.of(nowGame.whiteUserIpHash),
                    black = ChessPlayerInfo.of(nowGame.blackUserIpHash),
                    nowTurn = userTurn.ofReverse(),
                    newFen = nowFen,
                    move = move,
                    pgn = getMoveSan(prevFen, nowFen),
                )
            },
            movetimeMs = 500,
            elo = nowGame.botELO!!
        ).bestMove
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun closeAllGame(userIp: String) {
        chessStateService.endAllGame(ChessPlayerInfo.User.fromIp(userIp))
    }

    @Transactional(readOnly = true)
    suspend fun getNowPgn(userIp: String): String? {
        val nowGame = chessStateService.getPlaying(ChessPlayerInfo.User.fromIp(userIp)) ?: return null

        val gameLogs = chessStateService.getAllGameLog(nowGame.id.value)

        return gameLogs
            .chunked(2)
            .withIndex()
            .joinToString(" ") { (index, logs) ->
                "${index + 1}. ${logs.first().pgn}${logs.getOrNull(1)?.let { " ${it.pgn}" } ?: ""}"
            }
    }


}