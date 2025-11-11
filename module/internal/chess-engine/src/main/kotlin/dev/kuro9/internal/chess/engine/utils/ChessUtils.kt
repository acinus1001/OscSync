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