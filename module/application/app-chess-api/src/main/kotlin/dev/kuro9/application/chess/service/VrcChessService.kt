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

        val before = parseBoard(fenBefore)
        val after = parseBoard(fenAfter)

        val changes = mutableListOf<Triple<Int, Int, Char>>() // (row, col, piece)
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val b = before[r][c]
                val a = after[r][c]
                if (b != a) changes.add(Triple(r, c, b))
            }
        }

        // 🩵 Step 1: 킹 이동 우선 탐지
        var from: Pair<Int, Int>? = null
        var to: Pair<Int, Int>? = null
        var movedPiece = ' '

        // 먼저 킹 이동이 있는지 확인
        run breaking@{
            for ((r, c, b) in changes) {
                if (b.uppercaseChar() == 'K') {
                    from = r to c
                    movedPiece = b
                    // 이동 후 위치 찾기
                    for (rr in 0 until 8) {
                        for (cc in 0 until 8) {
                            if (after[rr][cc] == b) {
                                if (rr != r || cc != c) {
                                    to = rr to cc
                                    return@breaking
                                }
                            }
                        }
                    }
                }
            }
        }

        // 킹 이동이 없었다면 기존 방식으로 탐지
        if (from == null || to == null) {
            for (r in 0 until 8) {
                for (c in 0 until 8) {
                    val b = before[r][c]
                    val a = after[r][c]
                    if (b != a) {
                        if (b != '.' && (a == '.' || b.isUpperCase() != a.isUpperCase())) {
                            from = r to c
                            movedPiece = b
                        }
                        if (a != '.' && (b == '.' || b.isUpperCase() != a.isUpperCase())) {
                            to = r to c
                        }
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

        // 2️⃣ 캐슬링 감지 (킹 기준으로만)
        if (movedPiece.uppercaseChar() == 'K') {
            val isWhite = movedPiece.isUpperCase()
            val row = if (isWhite) 7 else 0

            // 킹사이드 캐슬링
            if (fromC == 4 && toC == 6 &&
                before[row][7].uppercaseChar() == 'R' &&
                after[row][5].uppercaseChar() == 'R'
            ) return "O-O"

            // 퀸사이드 캐슬링
            if (fromC == 4 && toC == 2 &&
                before[row][0].uppercaseChar() == 'R' &&
                after[row][3].uppercaseChar() == 'R'
            ) return "O-O-O"
        }

        // 3️⃣ 캡처 / 앙파상 / 프로모션 등 (이전 버전 그대로)
        val isWhite = movedPiece.isUpperCase()
        val dir = if (isWhite) -1 else 1

        var isEnPassant = false
        if (movedPiece.uppercaseChar() == 'P') {
            if (before[toR][toC] == '.' && fromC != toC) {
                val capturedR = toR - dir
                if (capturedR in 0..7 &&
                    before[capturedR][toC].uppercaseChar() == 'P' &&
                    after[capturedR][toC] == '.'
                ) {
                    isEnPassant = true
                }
            }
        }

        val isCapture = isEnPassant ||
                (before[toR][toC] != '.' && before[toR][toC].isUpperCase() != movedPiece.isUpperCase())

        val isPromotion = movedPiece.uppercaseChar() == 'P' &&
                ((isWhite && toR == 0) || (!isWhite && toR == 7))

        val promotedPiece = if (isPromotion) {
            val bPieces = after[toR][toC]
            if (bPieces.uppercaseChar() != 'P') bPieces.uppercaseChar().toString()
            else "Q"
        } else ""

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