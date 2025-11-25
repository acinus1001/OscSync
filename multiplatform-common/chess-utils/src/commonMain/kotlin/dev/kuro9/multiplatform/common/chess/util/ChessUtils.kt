package dev.kuro9.multiplatform.common.chess.util

import dev.kuro9.multiplatform.common.chess.util.internal.*
import kotlin.math.abs
import kotlin.math.sign

/**
 * FEN(포지션 필드만) -> 콘솔 출력용 체스판 문자열 생성
 *
 * @param fen 전체 FEN 문자열 또는 첫 필드(판배치)만. (예: "r1bqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
 * @param useUnicode true면 유니코드 기물 기호 사용, false면 ASCII 스타일(영문 기물) 사용
 * @throws IllegalArgumentException 유효하지 않은 FEN일 경우
 * @return 콘솔 출력용 멀티라인 문자열
 */
fun fenToBoardString(fen: String, useUnicode: Boolean = true): String {
    // FEN의 첫 필드만 취한다 (공백 전까지)
    val placement = fen.trim().split("\\s+".toRegex())[0]

    val ranks = placement.split('/')
    require(ranks.size == 8) { "FEN의 랭크 개수는 8개여야 합니다. 받은 랭크 수: ${ranks.size}" }

    // 유니코드/ASCII 맵
    val pieceMap = if (useUnicode) {
        mapOf(
            'K' to "♔", 'Q' to "♕", 'R' to "♖", 'B' to "♗", 'N' to "♘", 'P' to "♙",
            'k' to "♚", 'q' to "♛", 'r' to "♜", 'b' to "♝", 'n' to "♞", 'p' to "♟"
        )
    } else {
        mapOf(
            'K' to "K", 'Q' to "Q", 'R' to "R", 'B' to "B", 'N' to "N", 'P' to "P",
            'k' to "k", 'q' to "q", 'r' to "r", 'b' to "b", 'n' to "n", 'p' to "p"
        )
    }

    // 한 랭크(가로줄)를 파일 a..h (8개 칸)으로 변환
    fun expandRank(rank: String): List<String> {
        val row = mutableListOf<String>()
        for (ch in rank) {
            when {
                ch.isDigit() -> {
                    val count = ch.digitToInt()
                    repeat(count) { row.add(".") } // 빈칸 표시는 "."
                }

                ch.isLetter() -> {
                    val s = pieceMap[ch] ?: throw IllegalArgumentException("알 수 없는 기물 문자: $ch")
                    row.add(s)
                }

                else -> throw IllegalArgumentException("유효하지 않은 문자 in FEN: '$ch'")
            }
        }
        if (row.size != 8) throw IllegalArgumentException("랭크 '$rank'을(를) 확장한 결과 칸 수가 8이 아님: ${row.size}")
        return row
    }

    // 모든 랭크를 변환 (FEN은 8번째 랭크가 먼저(8) -> 마지막(1) 순서)
    val boardRows = ranks.map { expandRank(it) }

    // 출력 문자열 빌드
    return buildString {
        val fileLabels = ('a'..'h').toList()

        // 상단 여백(선택적) — 보드 상단 구분선
        append("  +------------------------+\n") // 8칸 * 3(각 칸 "X ") = 24, 맞춰서 라인 길이 조절

        for (i in 0 until 8) {
            val rankNumber = 8 - i
            append("$rankNumber |") // rank label

            // 각 칸은 한 칸당 3문자 필드(width 3)로 맞춤 (기호가 1문자일 때 가운데 정렬 비슷하게)
            for (j in 0 until 8) {
                val cell = boardRows[i][j]
                // 셀이 하나의 유니코드 문자면 가운데 정렬; '.'일 때도 " . "
                val cellStr = when {
                    cell == "." -> " . "
                    cell.length == 1 -> " ${cell} "
                    else -> { // 유니코드 기호는 대부분 1 grapheme이지만 string length가 1 이상일 수 있음 -> 공간 맞춤
                        val padded = cell.padStart((3 + cell.length) / 2).padEnd(3)
                        padded
                    }
                }
                append(cellStr)
            }
            append("|\n")
        }

        append("  +------------------------+\n")
        // 파일 라벨
        append("    ")
        for (f in fileLabels) {
            append(" $f ")
        }
        append("\n")
    }
}

/**
 * 두 FEN 문자열을 비교하여 체스 이동을 SAN(Standard Algebraic Notation) 형식으로 반환합니다.
 * (체크/체크메이트 '+' '#' 표기는 제외)
 *
 * @param fenBefore 이동 전 FEN 문자열.
 * @param fenAfter 이동 후 FEN 문자열.
 * @return SAN 형식의 이동 문자열.
 */
fun getMoveSan(fenBefore: String, fenAfter: String): String {
    // 보드 정보를 파싱하여 2차원 배열로 변환
    fun parseBoard(fen: String): Array<CharArray> {
        val board = Array(8) { CharArray(8) { '.' } }
        val parts = fen.split(" ")
        val rows = parts[0].split("/")

        for (r in 0 until 8) {
            var c = 0
            for (char in rows[r]) {
                if (char.isDigit()) {
                    repeat(char.digitToInt()) {
                        if (c < 8) board[r][c++] = '.'
                    }
                } else {
                    if (c < 8) board[r][c++] = char
                }
            }
        }
        return board
    }

    // 좌표를 체스 표기법으로 변환 (예: 0,0 -> "a8")
    fun getSquare(r: Int, c: Int): String {
        val file = 'a' + c
        val rank = 8 - r
        return "$file$rank"
    }

    /**
     * 경로가 비어있는지 확인하여, 다른 기물이 '실제로' 목적지로 이동 가능한지 판별합니다.
     */
    fun isPathClear(board: Array<CharArray>, piece: Char, from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val (fromR, fromC) = from
        val (toR, toC) = to
        val pieceType = piece.uppercaseChar()

        return when (pieceType) {
            ChessPiece.KNIGHT -> { // 나이트는 경로 방해의 개념이 없음
                abs((fromR - toR) * (fromC - toC)) == 2
            }

            ChessPiece.ROOK, ChessPiece.BISHOP, ChessPiece.QUEEN -> {
                val dr = (toR - fromR).sign
                val dc = (toC - fromC).sign

                // 이동 경로가 직선/대각선이 아니면 false
                if (fromR != toR && fromC != toC && abs(fromR - toR) != abs(fromC - toC)) return false

                // 룩이 대각선으로, 비숍이 직선으로 가려 하면 false
                if (pieceType == ChessPiece.ROOK && fromR != toR && fromC != toC) return false
                if (pieceType == ChessPiece.BISHOP && (fromR == toR || fromC == toC)) return false

                // 경로상에 다른 기물이 있는지 확인
                var r = fromR + dr
                var c = fromC + dc
                while (r != toR || c != toC) {
                    if (board[r][c] != '.') return false // 경로상에 다른 기물이 있으면 방해됨
                    r += dr
                    c += dc
                }
                true
            }

            else -> false
        }
    }

    /**
     * 다른 기물이 같은 목적지로 '합법적으로' 이동할 수 있는지 확인하여,
     * 필요한 경우에만 모호성을 해결할 표기(파일, 랭크)를 찾습니다.
     */
    fun findDisambiguation(board: Array<CharArray>, piece: Char, from: Pair<Int, Int>, to: Pair<Int, Int>): String {
        val (fromR, fromC) = from
        val ambiguousCandidates = mutableListOf<Pair<Int, Int>>()

        // 1. 현재 움직인 기물과 같은 종류의 다른 모든 기물을 찾습니다.
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                if ((r == fromR && c == fromC) || board[r][c] != piece) continue

                // 2. 후보 기물이 목적지로 '실제로' 이동 가능한지(경로가 비었는지) 확인합니다.
                if (isPathClear(board, piece, r to c, to)) {
                    ambiguousCandidates.add(r to c)
                }
            }
        }

        // 3. '실제로 위협적인' 후보가 없으면, disambiguation이 필요 없습니다.
        if (ambiguousCandidates.isEmpty()) {
            return ""
        }

        // 4. 후보가 있다면, 최소한의 정보로 disambiguation을 수행합니다.
        val needsFile = ambiguousCandidates.any { it.second == fromC }
        val needsRank = ambiguousCandidates.any { it.first == fromR }

        return when {
            needsFile && needsRank -> getSquare(fromR, fromC)
            needsFile -> (8 - fromR).toString() // 파일이 같으니 랭크로 구분
            else -> ('a' + fromC).toString() // 파일이 다르거나, 파일/랭크 모두 다르면 파일로 구분 (규칙)
        }
    }

    // --- Main Logic ---
    val before = parseBoard(fenBefore)
    val after = parseBoard(fenAfter)
    val fenPartsBefore = fenBefore.split(" ")

    val isWhiteMove = fenPartsBefore[1] == "w"

    // 1. 캐슬링 우선 감지
    val kingChar = if (isWhiteMove) ChessPiece.KING else ChessPiece.KING.lowercaseChar()

    // 킹의 위치 찾기
    val kingPositions = findKingPositions(before, after, kingChar)
    if (kingPositions != null) {
        val (kingFromC, kingToC) = kingPositions
        if (kingToC - kingFromC == 2) return "O-O" // 킹사이드 캐슬링
        if (kingToC - kingFromC == -2) return "O-O-O" // 퀸사이드 캐슬링
    }

    // 2. 일반 이동 감지
    val movementInfo = findMovementInfo(before, after, isWhiteMove) ?: return "?"
    val (from, to) = movementInfo

    val (fromR, fromC) = from
    val (toR, toC) = to

    val movedPiece = before[fromR][fromC]
    val capturedPiece = before[toR][toC]

    // 기물 문자 결정
    val pieceChar = when (movedPiece.uppercaseChar()) {
        ChessPiece.PAWN -> "" // 폰은 SAN에서 생략
        else -> movedPiece.uppercaseChar().toString()
    }

    // 3. 세부사항 판별
    val isPawnMove = movedPiece.uppercaseChar() == ChessPiece.PAWN
    val isCapture = capturedPiece != '.'
    val enPassantTarget = fenPartsBefore.getOrNull(3)
    val isEnPassant = isPawnMove && !isCapture && fromC != toC &&
            enPassantTarget != "-" && getSquare(toR, toC) == enPassantTarget
    val isPromotion = isPawnMove && (toR == 0 || toR == 7)
    val promotionPiece = if (isPromotion) after[toR][toC].uppercaseChar() else ' '

    // 4. 모호성 해결 (Disambiguation)
    val disambiguation = if (isPawnMove || movedPiece.uppercaseChar() == ChessPiece.KING) ""
    else findDisambiguation(before, movedPiece, from, to)

    // 5. SAN 문자열 조합
    return buildString {
        if (isPawnMove) {
            if (isCapture || isEnPassant) append('a' + fromC) // 폰 캡처 시 출발 파일 표시
        } else {
            append(pieceChar)
            append(disambiguation)
        }
        if (isCapture || isEnPassant) append('x')
        append(getSquare(toR, toC))
        if (isPromotion) append("=$promotionPiece")
    }
}

/**
 * 초기 FEN 문자열과 PGN 이동 목록을 받아 최종 FEN 문자열을 계산합니다.
 *
 * @param initialFen 시작 체스판 상태를 나타내는 FEN 문자열 (예: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
 * @param pgnList 적용할 체스 이동의 PGN 표기 목록 (예: ["e4", "d5", "exd5"])
 * @return 모든 이동 후의 최종 FEN 문자열
 * @throws IllegalArgumentException 유효하지 않은 이동이 포함된 경우
 */
fun getFen(initialFen: String, pgnList: List<String>): String {
    // FEN을 체스보드 객체로 파싱
    fun parseFen(fen: String): ChessBoard {
        val parts = fen.split(" ")
        val board = Array(8) { CharArray(8) { '.' } }

        // 보드 파싱
        val rows = parts[0].split("/")
        for (r in rows.indices) {
            var c = 0
            for (ch in rows[r]) {
                if (ch.isDigit()) {
                    // 빈 칸 개수
                    repeat(ch.digitToInt()) {
                        if (c < 8) board[r][c++] = '.'
                    }
                } else {
                    if (c < 8) board[r][c++] = ch
                }
            }
        }

        // 게임 상태 정보 파싱
        val gameInfo = mapOf(
            "turn" to (if (parts.size > 1) parts[1] else "w"),
            "castling" to (if (parts.size > 2) parts[2] else "KQkq"),
            "enPassant" to (if (parts.size > 3) parts[3] else "-"),
            "halfMoveClock" to (if (parts.size > 4) parts[4] else "0"),
            "fullMoveNumber" to (if (parts.size > 5) parts[5] else "1")
        )

        return ChessBoard(board, gameInfo)
    }

    // 보드 정보를 FEN 문자열로 변환
    fun boardToFen(chessBoard: ChessBoard): String {
        val board = chessBoard.board
        val gameInfo = chessBoard.gameInfo

        return buildString {
            // 보드 상태를 FEN 표기로 변환
            for (row in board) {
                var emptyCount = 0
                for (cell in row) {
                    if (cell == '.') {
                        emptyCount++
                    } else {
                        if (emptyCount > 0) {
                            append(emptyCount)
                            emptyCount = 0
                        }
                        append(cell)
                    }
                }
                if (emptyCount > 0) {
                    append(emptyCount)
                }
                append("/")
            }

            // 마지막 슬래시 제거
            deleteAt(length - 1)

            // 게임 정보 추가
            append(" ").append(gameInfo["turn"])
            append(" ").append(gameInfo["castling"])
            append(" ").append(gameInfo["enPassant"])
            append(" ").append(gameInfo["halfMoveClock"])
            append(" ").append(gameInfo["fullMoveNumber"])
        }
    }

    // PGN을 체스 이동 객체로 변환
    fun parsePgn(pgn: String, chessBoard: ChessBoard): ChessMove {
        val board = chessBoard.board
        val gameInfo = chessBoard.gameInfo
        val isWhiteTurn = gameInfo["turn"] == "w"

        // 체크(+)와 체크메이트(#) 표시 제거
        var cleanedPgn = pgn
            .replace("+", "")
            .replace("#", "")
            .replace("!", "")
            .replace("?", "")

        // 캐슬링 처리
        if (cleanedPgn == "O-O" || cleanedPgn == "0-0") { // 킹 사이드 캐슬링
            val rank = if (isWhiteTurn) 7 else 0
            return ChessMove(Pair(rank, 4), Pair(rank, 6))
        }
        if (cleanedPgn == "O-O-O" || cleanedPgn == "0-0-0") { // 퀸 사이드 캐슬링
            val rank = if (isWhiteTurn) 7 else 0
            return ChessMove(Pair(rank, 4), Pair(rank, 2))
        }

        // 프로모션 확인
        var promotion: Char? = null
        if (cleanedPgn.contains('=')) {
            val parts = cleanedPgn.split('=')
            cleanedPgn = parts[0]
            promotion = parts[1].first()
        }

        // 캡처 표시 제거
        cleanedPgn = cleanedPgn.replace("x", "")

        // 목적지 좌표 추출
        val toFile = cleanedPgn[cleanedPgn.length - 2]
        val toRank = cleanedPgn[cleanedPgn.length - 1]
        val toC = fileToIndex(toFile)
        val toR = rankToIndex(toRank)

        // 남은 부분 분석
        val remaining = cleanedPgn.substring(0, cleanedPgn.length - 2)

        // 기물 종류와 출발지 힌트 결정
        val (pieceType, fromFileHint, fromRankHint) = extractMoveDetails(remaining)

        // 출발지 찾기
        val fromPos = findMoveSource(
            board, isWhiteTurn, pieceType,
            toR, toC, fromFileHint, fromRankHint,
            gameInfo["enPassant"]
        )

        return ChessMove(fromPos, Pair(toR, toC), promotion)
    }

    // 이동을 적용하여 새로운 체스보드 상태 생성
    fun applyMove(chessBoard: ChessBoard, move: ChessMove): ChessBoard {
        // 캐슬링 시 룩 이동 처리
        fun applyKingCastlingMove(board: Array<CharArray>, rank: Int, fromC: Int, toC: Int) {
            if (toC > fromC) { // 킹 사이드 캐슬링
                board[rank][5] = board[rank][7] // 룩 이동
                board[rank][7] = '.'
            } else { // 퀸 사이드 캐슬링
                board[rank][3] = board[rank][0] // 룩 이동
                board[rank][0] = '.'
            }
        }

        // 이동 후 게임 정보 업데이트
        fun updateGameInfo(
            gameInfo: Map<String, String>,
            movedPiece: Char,
            fromR: Int,
            fromC: Int,
            toR: Int,
            toC: Int,
            capturedPiece: Char
        ): Map<String, String> {

            // 앙파상 타겟 업데이트
            fun updateEnPassantTarget(
                gameInfo: MutableMap<String, String>,
                movedPiece: Char,
                fromR: Int,
                toR: Int,
                toC: Int,
                isWhiteTurn: Boolean
            ) {
                if (movedPiece.uppercaseChar() == ChessPiece.PAWN) {
                    val rankDiff = abs(fromR - toR)
                    if (rankDiff == 2) {
                        val enPassantRank = (fromR + toR) / 2 // 중간 랭크
                        val file = ('a' + toC).toString()
                        val rank = 8 - enPassantRank
                        gameInfo["enPassant"] = "$file$rank"
                    }
                }
            }

            // 캐슬링 권한 업데이트
            fun updateCastlingRights(
                gameInfo: MutableMap<String, String>,
                movedPiece: Char,
                fromR: Int,
                fromC: Int,
                toR: Int,
                toC: Int,
                capturedPiece: Char,
                isWhiteTurn: Boolean
            ) {
                var castling = gameInfo["castling"] ?: "KQkq"

                // 킹 이동 시 해당 색상의 캐슬링 권한 모두 제거
                if (movedPiece.uppercaseChar() == ChessPiece.KING) {
                    if (isWhiteTurn) {
                        castling = castling.replace("K", "").replace("Q", "")
                    } else {
                        castling = castling.replace("k", "").replace("q", "")
                    }
                }
                // 룩 이동 시 해당 측면의 캐슬링 권한만 제거
                else if (movedPiece.uppercaseChar() == ChessPiece.ROOK) {
                    if (isWhiteTurn) {
                        if (fromR == 7 && fromC == 0) castling = castling.replace("Q", "")
                        if (fromR == 7 && fromC == 7) castling = castling.replace("K", "")
                    } else {
                        if (fromR == 0 && fromC == 0) castling = castling.replace("q", "")
                        if (fromR == 0 && fromC == 7) castling = castling.replace("k", "")
                    }
                }

                // 룩이 잡힌 경우에도 캐슬링 권한 업데이트
                if (capturedPiece.uppercaseChar() == ChessPiece.ROOK) {
                    if (toR == 0 && toC == 0) castling = castling.replace("q", "")
                    if (toR == 0 && toC == 7) castling = castling.replace("k", "")
                    if (toR == 7 && toC == 0) castling = castling.replace("Q", "")
                    if (toR == 7 && toC == 7) castling = castling.replace("K", "")
                }

                gameInfo["castling"] = if (castling.isEmpty()) "-" else castling
            }

            // 하프무브 클럭 업데이트
            fun updateHalfMoveClock(
                gameInfo: MutableMap<String, String>,
                movedPiece: Char,
                capturedPiece: Char
            ) {
                val halfMoveClock = gameInfo["halfMoveClock"]?.toIntOrNull() ?: 0

                // 폰 이동이나 캡처가 있으면 리셋, 아니면 증가
                if (movedPiece.uppercaseChar() == ChessPiece.PAWN || capturedPiece != '.') {
                    gameInfo["halfMoveClock"] = "0"
                } else {
                    gameInfo["halfMoveClock"] = (halfMoveClock + 1).toString()
                }
            }

            // 풀무브 넘버 업데이트
            fun updateFullMoveNumber(gameInfo: MutableMap<String, String>, isWhiteTurn: Boolean) {
                if (!isWhiteTurn) {
                    val fullMoveNumber = gameInfo["fullMoveNumber"]?.toIntOrNull() ?: 1
                    gameInfo["fullMoveNumber"] = (fullMoveNumber + 1).toString()
                }
            }

            val isWhiteTurn = gameInfo["turn"] == "w"
            val mutableGameInfo = gameInfo.toMutableMap()

            // 턴 변경
            mutableGameInfo["turn"] = if (isWhiteTurn) "b" else "w"

            // 앙파상 타겟 업데이트
            mutableGameInfo["enPassant"] = "-"
            updateEnPassantTarget(mutableGameInfo, movedPiece, fromR, toR, toC, isWhiteTurn)

            // 캐슬링 권한 업데이트
            updateCastlingRights(mutableGameInfo, movedPiece, fromR, fromC, toR, toC, capturedPiece, isWhiteTurn)

            // 하프무브 클럭 업데이트
            updateHalfMoveClock(mutableGameInfo, movedPiece, capturedPiece)

            // 풀무브 넘버 업데이트
            updateFullMoveNumber(mutableGameInfo, isWhiteTurn)

            return mutableGameInfo
        }

        val board = chessBoard.board
        val gameInfo = chessBoard.gameInfo
        val isWhiteTurn = gameInfo["turn"] == "w"

        val (fromPos, toPos, promotion) = move
        val (fromR, fromC) = fromPos
        val (toR, toC) = toPos

        // 이동 적용
        val movedPiece = board[fromR][fromC]
        val capturedPiece = board[toR][toC]

        // 체스판 복사
        val newBoard = Array(8) { r -> board[r].copyOf() }

        // 캐슬링인 경우 룩도 이동
        if (movedPiece.uppercaseChar() == ChessPiece.KING && abs(fromC - toC) == 2) {
            applyKingCastlingMove(newBoard, toR, fromC, toC)
        }

        // 앙파상 처리
        if (movedPiece.uppercaseChar() == ChessPiece.PAWN && fromC != toC && board[toR][toC] == '.') {
            newBoard[fromR][toC] = '.' // 앙파상으로 잡힌 폰 제거
        }

        // 기본 이동
        newBoard[toR][toC] = if (promotion != null) {
            if (isWhiteTurn) promotion else promotion.lowercaseChar()
        } else {
            movedPiece
        }
        newBoard[fromR][fromC] = '.'

        // 게임 상태 정보 업데이트
        val newGameInfo = updateGameInfo(gameInfo, movedPiece, fromR, fromC, toR, toC, capturedPiece)

        return ChessBoard(newBoard, newGameInfo)
    }

    // PGN 이동을 적용
    fun applyPgnMove(fen: String, pgn: String): String {
        try {
            // 체크/체크메이트 표시 제거
            val cleanedPgn = pgn.replace("+", "").replace("#", "")

            val chessBoard = parseFen(fen)
            val move = parsePgn(cleanedPgn, chessBoard)
            val newBoard = applyMove(chessBoard, move)

            return boardToFen(newBoard)
        } catch (e: Exception) {
            throw IllegalArgumentException("이동 적용 실패: $pgn (${e.message})")
        }
    }

    // 초기 FEN에서 모든 PGN 이동을 순차적으로 적용
    var currentFen = initialFen
    for (pgn in pgnList) {
        currentFen = applyPgnMove(currentFen, pgn)
    }

    return currentFen
}

/**
 * 초기 FEN 문자열과 PGN 이동 목록을 받아 최종 FEN 문자열을 계산합니다.
 *
 * @param initialFen 시작 체스판 상태를 나타내는 FEN 문자열 (예: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
 * @param pgnList 적용할 체스 이동의 PGN 표기 (예: "1. e4 c5 2. Nf3 g6")
 * @return 모든 이동 후의 최종 FEN 문자열
 * @throws IllegalArgumentException 유효하지 않은 이동이 포함된 경우
 */
fun getFen(initialFen: String, pgn: String): String {
    val moves = pgn
        .replace(Regex("\\d+\\.+"), "") // Remove move numbers with any number of dots
        .trim()
        .split(Regex("\\s+")) // Split on whitespace
        .filter { it.isNotEmpty() } // Remove empty strings

    return getFen(initialFen, moves)
}

/**
 * 전체 PGN 문자열에서 SAN(Standard Algebraic Notation) 이동 목록 추출
 * 태그 섹션, 이동 번호, 주석, 결과 등을 모두 제거하고 순수 이동만 리스트로 반환
 *
 * @param fullPgn 전체 PGN 문자열
 * @return 순수 SAN 이동 목록(리스트)
 */
fun extractSanListFromPgn(fullPgn: String): List<String> {
    // 1. 태그 섹션 제거 [Event "?"] 등
    val withoutTags = fullPgn.replace("\\[.*?]\\s*".toRegex(), "")

    // 2. 결과 표시 제거 (1-0, 0-1, 1/2-1/2)
    val withoutResults = withoutTags.replace("\\s+(1-0|0-1|1/2-1/2)\\s*$".toRegex(), "")

    // 3. 중괄호 주석 제거 {like this}
    val withoutComments = withoutResults.replace("\\{[^}]*\\}".toRegex(), "")

    // 4. 이동 번호와 점 제거 (예: "1.", "2.")
    val withoutMoveNumbers = withoutComments.replace("\\d+\\.+\\s*".toRegex(), " ")

    // 5. 연속된 공백을 하나로 치환하고 앞뒤 공백 제거
    val cleaned = withoutMoveNumbers.replace("\\s+".toRegex(), " ").trim()

    // 6. 공백으로 분리하여 배열로 반환
    return cleaned.split(" ").filter { it.isNotEmpty() }
}
