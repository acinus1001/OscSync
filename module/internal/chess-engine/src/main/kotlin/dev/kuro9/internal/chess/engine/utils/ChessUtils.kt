package dev.kuro9.internal.chess.engine.utils

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
    val unicodeMap = mapOf(
        'K' to "♔", 'Q' to "♕", 'R' to "♖", 'B' to "♗", 'N' to "♘", 'P' to "♙",
        'k' to "♚", 'q' to "♛", 'r' to "♜", 'b' to "♝", 'n' to "♞", 'p' to "♟"
    )
    val asciiMap = mapOf(
        'K' to "K", 'Q' to "Q", 'R' to "R", 'B' to "B", 'N' to "N", 'P' to "P",
        'k' to "k", 'q' to "q", 'r' to "r", 'b' to "b", 'n' to "n", 'p' to "p"
    )
    val pieceMap = if (useUnicode) unicodeMap else asciiMap

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
    val sb = StringBuilder()
    val fileLabels = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')

    // 상단 여백(선택적) — 보드 상단 구분선
    sb.append("  +------------------------+\n") // 8칸 * 3(각 칸 "X ") = 24, 맞춰서 라인 길이 조절
    for (i in 0 until 8) {
        val rankIndex = i
        val rankNumber = 8 - i
        sb.append("$rankNumber |") // rank label
        // 각 칸은 한 칸당 3문자 필드(width 3)로 맞춤 (기호가 1문자일 때 가운데 정렬 비슷하게)
        for (j in 0 until 8) {
            val cell = boardRows[rankIndex][j]
            // 셀이 하나의 유니코드 문자면 가운데 정렬; '.'일 때도 " . "
            val cellStr = when {
                cell == "." -> " . "
                cell.length == 1 -> " ${cell} "
                else -> { // 유니코드 기호는 대부분 1 grapheme이지만 string length가 1 이상일 수 있음 -> 공간 맞춤
                    val padded = cell.padStart((3 + cell.length) / 2).padEnd(3)
                    padded
                }
            }
            sb.append(cellStr)
        }
        sb.append("|\n")
    }
    sb.append("  +------------------------+\n")
    // 파일 라벨
    sb.append("    ")
    for (f in fileLabels) {
        sb.append(" $f ")
    }
    sb.append("\n")
    return sb.toString()
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
    // --- Helper Functions ---

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

        when (pieceType) {
            'N' -> { // 나이트는 경로 방해의 개념이 없음
                return abs((fromR - toR) * (fromC - toC)) == 2
            }

            'R', 'B', 'Q' -> {
                val dr = (toR - fromR).sign
                val dc = (toC - fromC).sign

                // 이동 경로가 직선/대각선이 아니면 false
                if (fromR != toR && fromC != toC && abs(fromR - toR) != abs(fromC - toC)) return false
                // 룩이 대각선으로, 비숍이 직선으로 가려 하면 false
                if (pieceType == 'R' && fromR != toR && fromC != toC) return false
                if (pieceType == 'B' && (fromR == toR || fromC == toC)) return false

                var r = fromR + dr
                var c = fromC + dc
                while (r != toR || c != toC) {
                    if (board[r][c] != '.') return false // 경로상에 다른 기물이 있으면 방해됨
                    r += dr
                    c += dc
                }
                return true
            }
        }
        return false
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
    val kingChar = if (isWhiteMove) 'K' else 'k'
    var kingFromC: Int? = null
    var kingToC: Int? = null
    for (r in 0 until 8) {
        for (c in 0 until 8) {
            if (before[r][c] == kingChar) kingFromC = c
            if (after[r][c] == kingChar) kingToC = c
        }
    }

    if (kingFromC != null && kingToC != null) {
        if (kingToC - kingFromC == 2) return "O-O"
        if (kingToC - kingFromC == -2) return "O-O-O"
    }

    // 2. 일반 이동 감지
    var from: Pair<Int, Int>? = null
    var to: Pair<Int, Int>? = null

    for (r in 0 until 8) {
        for (c in 0 until 8) {
            val pieceBefore = before[r][c]
            val pieceAfter = after[r][c]
            if (pieceBefore != pieceAfter) {
                if (pieceBefore != '.' && pieceBefore.isUpperCase() == isWhiteMove) from = r to c
                if (pieceAfter != '.' && pieceAfter.isUpperCase() == isWhiteMove) to = r to c
            }
        }
    }

    if (from == null || to == null) return "?"

    val (fromR, fromC) = from
    val (toR, toC) = to

    val movedPiece = before[fromR][fromC]
    val capturedPiece = before[toR][toC]

    val pieceChar = when (movedPiece.uppercaseChar()) {
        'P' -> ""
        else -> movedPiece.uppercaseChar().toString()
    }

    // 3. 세부사항 판별
    val isPawnMove = movedPiece.uppercaseChar() == 'P'
    val isCapture = capturedPiece != '.'
    val enPassantTarget = fenPartsBefore.getOrNull(3)
    val isEnPassant = isPawnMove && !isCapture && fromC != toC &&
            enPassantTarget != "-" && getSquare(toR, toC) == enPassantTarget
    val isPromotion = isPawnMove && (toR == 0 || toR == 7)
    val promotionPiece = if (isPromotion) after[toR][toC].uppercaseChar() else ' '

    // 4. 모호성 해결 (Disambiguation)
    val disambiguation = if (isPawnMove || movedPiece.uppercaseChar() == 'K') ""
    else findDisambiguation(before, movedPiece, from, to)

    // 5. SAN 문자열 조합
    return buildString {
        if (isPawnMove) {
            if (isCapture || isEnPassant) append('a' + fromC)
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
    // PGN 표기법에서 사용되는 파일(열) 문자를 인덱스로 변환
    fun fileToIndex(file: Char): Int = file - 'a'

    // PGN 표기법에서 사용되는 랭크(행) 숫자를 인덱스로 변환
    fun rankToIndex(rank: Char): Int = 8 - rank.digitToInt()

    // FEN을 8x8 보드와 기타 게임 정보로 파싱
    fun parseFen(fen: String): Pair<Array<CharArray>, MutableMap<String, String>> {
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
        val gameInfo = mutableMapOf(
            "turn" to (if (parts.size > 1) parts[1] else "w"),
            "castling" to (if (parts.size > 2) parts[2] else "KQkq"),
            "enPassant" to (if (parts.size > 3) parts[3] else "-"),
            "halfMoveClock" to (if (parts.size > 4) parts[4] else "0"),
            "fullMoveNumber" to (if (parts.size > 5) parts[5] else "1")
        )

        return Pair(board, gameInfo)
    }

    // 보드 정보를 FEN 문자열로 변환
    fun boardToFen(board: Array<CharArray>, gameInfo: Map<String, String>): String {
        val sb = StringBuilder()

        // 보드 상태를 FEN 표기로 변환
        for (row in board) {
            var emptyCount = 0
            for (cell in row) {
                if (cell == '.') {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    sb.append(cell)
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            sb.append("/")
        }

        // 마지막 슬래시 제거
        sb.deleteCharAt(sb.length - 1)

        // 게임 정보 추가
        sb.append(" ").append(gameInfo["turn"])
        sb.append(" ").append(gameInfo["castling"])
        sb.append(" ").append(gameInfo["enPassant"])
        sb.append(" ").append(gameInfo["halfMoveClock"])
        sb.append(" ").append(gameInfo["fullMoveNumber"])

        return sb.toString()
    }

    // PGN에서 이동 정보를 추출
    fun parsePgn(
        pgn: String,
        isWhiteTurn: Boolean,
        board: Array<CharArray>,
        gameInfo: Map<String, String>
    ): Triple<Pair<Int, Int>, Pair<Int, Int>, Char?> {
        // 기물 경로상 다른 기물이 있는지 확인
        fun isPiecePathClear(board: Array<CharArray>, fromR: Int, fromC: Int, toR: Int, toC: Int): Boolean {
            val rDiff = toR - fromR
            val cDiff = toC - fromC

            // 직선 이동이 아니면 false (룩, 비숍, 퀸에게 적용)
            if (rDiff != 0 && cDiff != 0 && abs(rDiff) != abs(cDiff)) return false

            val rStep = if (rDiff == 0) 0 else rDiff / abs(rDiff)
            val cStep = if (cDiff == 0) 0 else cDiff / abs(cDiff)

            var r = fromR + rStep
            var c = fromC + cStep

            while (r != toR || c != toC) {
                if (board[r][c] != '.') return false // 경로상 다른 기물이 있으면 막힘
                r += rStep
                c += cStep
            }

            return true
        }

        // 캐슬링 처리
        if (pgn == "O-O") { // 킹 사이드 캐슬링
            val rank = if (isWhiteTurn) 7 else 0
            return Triple(Pair(rank, 4), Pair(rank, 6), null)
        }
        if (pgn == "O-O-O") { // 퀸 사이드 캐슬링
            val rank = if (isWhiteTurn) 7 else 0
            return Triple(Pair(rank, 4), Pair(rank, 2), null)
        }

        // 프로모션 확인
        var promotion: Char? = null
        var pgn = pgn
        if (pgn.contains('=')) {
            val parts = pgn.split('=')
            pgn = parts[0]
            promotion = parts[1].first()
        }

        // 캡처 표시 제거
        pgn = pgn.replace("x", "")

        // 목적지 좌표 추출
        val toFile = pgn[pgn.length - 2]
        val toRank = pgn[pgn.length - 1]
        val toC = fileToIndex(toFile)
        val toR = rankToIndex(toRank)

        // 남은 부분 분석
        val remaining = pgn.substring(0, pgn.length - 2)

        // 기물 종류 결정
        var pieceType = 'P' // 기본값은 폰
        var fromFileHint: Char? = null
        var fromRankHint: Char? = null

        if (remaining.isNotEmpty()) {
            // 첫 글자가 대문자면 기물 종류
            if (remaining[0].isUpperCase() && remaining[0] != 'O') {
                pieceType = remaining[0]

                // 명확화를 위한 힌트가 있는지 확인
                if (remaining.length > 1) {
                    val disambiguationPart = remaining.substring(1)
                    for (ch in disambiguationPart) {
                        when (ch) {
                            in 'a'..'h' -> fromFileHint = ch
                            in '1'..'8' -> fromRankHint = ch
                        }
                    }
                }
            } else {
                // 폰 이동에서는 파일이 명시될 수 있음
                if (remaining.length == 1 && remaining[0] in 'a'..'h') {
                    fromFileHint = remaining[0]
                }
            }
        }

        // 출발지 찾기
        val possibleStarts = mutableListOf<Pair<Int, Int>>()

        // 폰 특수 케이스
        if (pieceType == 'P') {
            val direction = if (isWhiteTurn) -1 else 1 // 폰 이동 방향
            val pawnChar = if (isWhiteTurn) 'P' else 'p'

            // 앙파상 가능성 확인
            val enPassantTarget = gameInfo["enPassant"]
            val isEnPassant = enPassantTarget != "-" && enPassantTarget != null &&
                    "${toFile}${toRank}" == enPassantTarget

            if (fromFileHint != null) {
                // 캡처 이동: 대각선으로 이동
                val fromC = fileToIndex(fromFileHint)
                val fromR = toR - direction

                // 범위 확인 및 폰 존재 여부 확인
                if (fromR in 0..7 && fromC in 0..7 && board[fromR][fromC] == pawnChar) {
                    possibleStarts.add(Pair(fromR, fromC))
                }
            } else {
                // 일반 이동: 전방 이동
                // 한 칸 이동
                val fromR1 = toR - direction
                if (fromR1 in 0..7 && board[fromR1][toC] == pawnChar) {
                    possibleStarts.add(Pair(fromR1, toC))
                }

                // 두 칸 이동 (시작 위치에서만)
                val startRank = if (isWhiteTurn) 6 else 1
                val fromR2 = toR - 2 * direction
                if (fromR2 == startRank && board[fromR2][toC] == pawnChar &&
                    board[fromR1][toC] == '.'
                ) {
                    possibleStarts.add(Pair(fromR2, toC))
                }
            }
        } else {
            // 다른 기물 이동
            val piece = if (isWhiteTurn) pieceType else pieceType.lowercaseChar()

            for (r in 0..7) {
                for (c in 0..7) {
                    // 출발지에 해당 기물이 있는지 확인
                    if (board[r][c] != piece) continue

                    // 힌트가 있으면 적용
                    if (fromFileHint != null && c != fileToIndex(fromFileHint)) continue
                    if (fromRankHint != null && r != rankToIndex(fromRankHint)) continue

                    // 기물 유형에 따른 이동 가능성 확인
                    val canMove = when (pieceType) {
                        'R' -> r == toR || c == toC  // 룩: 같은 행 또는 열
                        'N' -> (abs(r - toR) == 1 && abs(c - toC) == 2) || (abs(r - toR) == 2 && abs(c - toC) == 1)  // 나이트: L자 이동
                        'B' -> abs(r - toR) == abs(c - toC)  // 비숍: 대각선
                        'Q' -> r == toR || c == toC || abs(r - toR) == abs(c - toC)  // 퀸: 직선 또는 대각선
                        'K' -> abs(r - toR) <= 1 && abs(c - toC) <= 1  // 킹: 주변 1칸
                        else -> false
                    }

                    if (canMove) {
                        // 경로상 다른 기물이 없는지 확인 (나이트 제외)
                        if (pieceType == 'N' || isPiecePathClear(board, r, c, toR, toC)) {
                            possibleStarts.add(Pair(r, c))
                        }
                    }
                }
            }
        }

        // 가능한 출발지가 없으면 예외 발생
        if (possibleStarts.isEmpty()) {
            throw IllegalArgumentException("유효하지 않은 PGN 이동: $pgn")
        }

        // 여러 가능성이 있으면 첫 번째 선택 (실제로는 더 정교한 로직이 필요할 수 있음)
        val fromPos = possibleStarts[0]

        return Triple(fromPos, Pair(toR, toC), promotion)
    }

    // PGN 이동을 적용하여 새 FEN 생성
    fun applyMove(fen: String, pgn: String): String {
        // FEN 분석
        val (board, gameInfo) = parseFen(fen)
        val isWhiteTurn = gameInfo["turn"] == "w"

        // PGN 분석하여 이동 정보 추출
        val (fromPos, toPos, promotion) = parsePgn(pgn, isWhiteTurn, board, gameInfo)
        val (fromR, fromC) = fromPos
        val (toR, toC) = toPos

        // 이동 적용
        val movedPiece = board[fromR][fromC]
        val capturedPiece = board[toR][toC]

        // 체스판 복사
        val newBoard = Array(8) { r -> board[r].copyOf() }

        // 캐슬링인 경우 룩도 이동
        if (movedPiece.uppercaseChar() == 'K' && abs(fromC - toC) == 2) {
            if (toC > fromC) { // 킹 사이드 캐슬링
                newBoard[toR][5] = newBoard[toR][7] // 룩 이동
                newBoard[toR][7] = '.'
            } else { // 퀸 사이드 캐슬링
                newBoard[toR][3] = newBoard[toR][0] // 룩 이동
                newBoard[toR][0] = '.'
            }
        }

        // 앙파상 처리
        if (movedPiece.uppercaseChar() == 'P' && fromC != toC && board[toR][toC] == '.') {
            val captureRank = fromR
            newBoard[captureRank][toC] = '.' // 앙파상으로 잡힌 폰 제거
        }

        // 기본 이동
        newBoard[toR][toC] = if (promotion != null) {
            if (isWhiteTurn) promotion else promotion.lowercaseChar()
        } else {
            movedPiece
        }
        newBoard[fromR][fromC] = '.'

        // 게임 상태 정보 업데이트
        val newGameInfo = gameInfo.toMutableMap()

        // 턴 변경
        newGameInfo["turn"] = if (isWhiteTurn) "b" else "w"

        // 앙파상 타겟 업데이트
        newGameInfo["enPassant"] = "-"
        if (movedPiece.uppercaseChar() == 'P') {
            val rankDiff = abs(fromR - toR)
            if (rankDiff == 2) {
                val enPassantRank = (fromR + toR) / 2 // 중간 랭크
                val file = ('a' + toC).toString()
                val rank = 8 - enPassantRank
                newGameInfo["enPassant"] = "$file$rank"
            }
        }

        // 캐슬링 권한 업데이트
        var castling = newGameInfo["castling"] ?: "KQkq"
        if (movedPiece.uppercaseChar() == 'K') {
            if (isWhiteTurn) {
                castling = castling.replace("K", "").replace("Q", "")
            } else {
                castling = castling.replace("k", "").replace("q", "")
            }
        } else if (movedPiece.uppercaseChar() == 'R') {
            if (isWhiteTurn) {
                if (fromR == 7 && fromC == 0) castling = castling.replace("Q", "")
                if (fromR == 7 && fromC == 7) castling = castling.replace("K", "")
            } else {
                if (fromR == 0 && fromC == 0) castling = castling.replace("q", "")
                if (fromR == 0 && fromC == 7) castling = castling.replace("k", "")
            }
        }
        // 룩이 잡힌 경우에도 캐슬링 권한 업데이트
        if (capturedPiece.uppercaseChar() == 'R') {
            if (toR == 0 && toC == 0) castling = castling.replace("q", "")
            if (toR == 0 && toC == 7) castling = castling.replace("k", "")
            if (toR == 7 && toC == 0) castling = castling.replace("Q", "")
            if (toR == 7 && toC == 7) castling = castling.replace("K", "")
        }

        newGameInfo["castling"] = if (castling.isEmpty()) "-" else castling

        // 하프무브 클럭 업데이트
        val halfMoveClock = gameInfo["halfMoveClock"]?.toIntOrNull() ?: 0
        if (movedPiece.uppercaseChar() == 'P' || capturedPiece != '.') {
            newGameInfo["halfMoveClock"] = "0"
        } else {
            newGameInfo["halfMoveClock"] = (halfMoveClock + 1).toString()
        }

        // 풀무브 넘버 업데이트
        if (!isWhiteTurn) {
            val fullMoveNumber = gameInfo["fullMoveNumber"]?.toIntOrNull() ?: 1
            newGameInfo["fullMoveNumber"] = (fullMoveNumber + 1).toString()
        }

        return boardToFen(newBoard, newGameInfo)
    }

    // 초기 FEN 및 모든 PGN 이동을 순차적으로 적용
    var currentFen = initialFen
    for (pgn in pgnList) {
        // 체크(+)와 체크메이트(#) 표시 제거
        val cleanedPgn = pgn.replace("+", "").replace("#", "")
        currentFen = applyMove(currentFen, cleanedPgn)
    }

    return currentFen
}

fun main() {
    val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    val pgnList = listOf(
        "e4", "e5",
        "Nf3", "Nc6",
        "Bc4", "Bc5",
        "c3", "Nf6",
        "d4", "exd4",
        "e5", "d5",
        "Bb5", "Ne4",
        "cxd4", "Be7",
        "Be3", "Nxf2",
        "Bxf2", "g5",
        "O-O", "Kf8",
        "Be3", "Nb8",
        "Nxg5", "Bxg5",
        "Rxf7+", "Kg8",
        "Bxg5", "Qxg5",
        "Qf3", "Qc1+",
        "Bf1", "Qe3+",
        "Qxe3", "a6",
        "e6", "Bxe6",
        "Qxe6", "Nd7",
        "Rxd7+", "Kf8",
        "Qf7#"
    )
    println(getFen(fen, pgnList))
}