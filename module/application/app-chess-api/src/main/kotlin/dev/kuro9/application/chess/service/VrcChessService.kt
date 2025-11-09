package dev.kuro9.application.chess.service

import dev.kuro9.application.chess.exception.GameNotExistException
import dev.kuro9.domain.chess.integration.vrc.dto.ChessPlayerInfo
import dev.kuro9.domain.chess.integration.vrc.enums.ChessTurn
import dev.kuro9.domain.chess.integration.vrc.service.VrcChessStateService
import dev.kuro9.internal.chess.engine.StockFishService
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

    private fun getMoveSan(fenBefore: String, fenAfter: String): String {
        fun parseBoard(fen: String): Array<CharArray> {
            val board = Array(8) { CharArray(8) }
            val rows = fen.split(" ")[0].split("/")
            for (r in 0 until 8) {
                var c = 0
                for (ch in rows[r]) {
                    if (ch.isDigit()) repeat(ch.digitToInt()) { board[r][c++] = '.' }
                    else board[r][c++] = ch
                }
            }
            return board
        }

        fun sqToStr(r: Int, c: Int): String = "${'a' + c}${8 - r}"

        val before = parseBoard(fenBefore)
        val after = parseBoard(fenAfter)

        var from: Pair<Int, Int>? = null
        var to: Pair<Int, Int>? = null
        var movedPiece = ' '

        // 1️⃣ 변경된 칸 탐색
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val b = before[r][c]
                val a = after[r][c]
                if (b != a) {
                    // 이동한 기물
                    if (b != '.' && (a == '.' || b.isUpperCase() != a.isUpperCase())) {
                        from = r to c
                        movedPiece = b
                    }
                    // 도착한 칸
                    if (a != '.' && (b == '.' || b.isUpperCase() != a.isUpperCase())) {
                        to = r to c
                    }
                }
            }
        }

        if (from == null || to == null) return "?"

        val (fromR, fromC) = from!!
        val (toR, toC) = to!!

        val fileFrom = ('a' + fromC)
        val rankFrom = 8 - fromR
        val fileTo = ('a' + toC)
        val rankTo = 8 - toR

        val pieceChar = when (movedPiece.uppercaseChar()) {
            'P' -> ""
            'N' -> "N"
            'B' -> "B"
            'R' -> "R"
            'Q' -> "Q"
            'K' -> "K"
            else -> ""
        }

        // 2️⃣ 캐슬링 감지
        if (movedPiece.uppercaseChar() == 'K' && kotlin.math.abs(toC - fromC) == 2) {
            return if (toC > fromC) "O-O" else "O-O-O"
        }

        // 3️⃣ 캡처 감지
        val isCapture = before[toR][toC] != '.' && before[toR][toC].isUpperCase() != movedPiece.isUpperCase()

        // 4️⃣ 프로모션 감지
        val isWhite = movedPiece.isUpperCase()
        val isPromotion = movedPiece.uppercaseChar() == 'P' &&
                ((isWhite && toR == 0) || (!isWhite && toR == 7))

        val promotedPiece = if (isPromotion) {
            val bPieces = after[toR][toC]
            if (bPieces.uppercaseChar() != 'P') bPieces.uppercaseChar().toString()
            else "Q" // 기본값
        } else ""

        // 5️⃣ SAN 조합
        val san = buildString {
            append(pieceChar)
            if (isCapture && movedPiece.uppercaseChar() == 'P') append(fileFrom)
            if (isCapture) append("x")
            append("$fileTo$rankTo")
            if (isPromotion) append("=${promotedPiece.uppercase()}")
        }

        return san
    }
}