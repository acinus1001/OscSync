package dev.kuro9.multiplatform.common.chess.util.internal

import kotlin.math.abs

/**
 * 체스 관련 데이터 모델
 */
internal data class ChessBoard(
    val board: Array<CharArray>,
    val gameInfo: Map<String, String>
) {
    // Array에 대한 equals와 hashCode 재정의
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChessBoard) return false

        return board.contentDeepEquals(other.board) && gameInfo == other.gameInfo
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + gameInfo.hashCode()
        return result
    }
}

/**
 * 체스 이동 데이터 모델
 */
internal data class ChessMove(
    val from: Pair<Int, Int>,
    val to: Pair<Int, Int>,
    val promotion: Char? = null
)

/**
 * 기물 종류 상수
 */
internal object ChessPiece {
    const val KING = 'K'
    const val QUEEN = 'Q'
    const val ROOK = 'R'
    const val BISHOP = 'B'
    const val KNIGHT = 'N'
    const val PAWN = 'P'

    fun isWhite(piece: Char): Boolean = piece.isUpperCase()
    fun getBaseType(piece: Char): Char = piece.uppercaseChar()
}


/**
 * 체스보드에서 킹의 위치를 찾습니다.
 */
internal fun findKingPositions(before: Array<CharArray>, after: Array<CharArray>, kingChar: Char): Pair<Int, Int>? {
    var kingFromC: Int? = null
    var kingToC: Int? = null
    var kingRow: Int? = null

    for (r in 0 until 8) {
        for (c in 0 until 8) {
            if (before[r][c] == kingChar) {
                kingFromC = c
                kingRow = r
            }
            if (after[r][c] == kingChar && (kingRow == null || r == kingRow)) {
                kingToC = c
            }
        }
    }

    return if (kingFromC != null && kingToC != null) kingFromC to kingToC else null
}

/**
 * 체스보드에서 이동한 기물의 출발지와 목적지를 찾습니다.
 */
internal fun findMovementInfo(
    before: Array<CharArray>,
    after: Array<CharArray>,
    isWhiteMove: Boolean
): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    var from: Pair<Int, Int>? = null
    var to: Pair<Int, Int>? = null

    for (r in 0 until 8) {
        for (c in 0 until 8) {
            val pieceBefore = before[r][c]
            val pieceAfter = after[r][c]
            if (pieceBefore != pieceAfter) {
                if (pieceBefore != '.' && ChessPiece.isWhite(pieceBefore) == isWhiteMove) from = r to c
                if (pieceAfter != '.' && ChessPiece.isWhite(pieceAfter) == isWhiteMove) to = r to c
            }
        }
    }

    return if (from != null && to != null) from to to else null
}


internal fun fileToIndex(file: Char): Int = file - 'a'
internal fun rankToIndex(rank: Char): Int = 8 - rank.digitToInt()
internal fun isInBoard(r: Int, c: Int): Boolean = r in 0..7 && c in 0..7

// 기물 경로상 장애물이 없는지 확인
internal fun isPiecePathClear(board: Array<CharArray>, fromR: Int, fromC: Int, toR: Int, toC: Int): Boolean {
    val rDiff = toR - fromR
    val cDiff = toC - fromC

    // 직선 이동이 아니면 false
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

// PGN 이동 정보에서 기물 타입과 출발지 힌트 추출
internal fun extractMoveDetails(moveText: String): Triple<Char, Char?, Char?> {
    var pieceType = ChessPiece.PAWN // 기본값은 폰
    var fromFileHint: Char? = null
    var fromRankHint: Char? = null

    if (moveText.isNotEmpty()) {
        // 첫 글자가 대문자면 기물 종류
        if (moveText[0].isUpperCase() && moveText[0] != 'O') {
            pieceType = moveText[0]

            // 명확화를 위한 힌트가 있는지 확인
            if (moveText.length > 1) {
                val disambiguationPart = moveText.substring(1)
                for (ch in disambiguationPart) {
                    when (ch) {
                        in 'a'..'h' -> fromFileHint = ch
                        in '1'..'8' -> fromRankHint = ch
                    }
                }
            }
        } else if (moveText.length == 1 && moveText[0] in 'a'..'h') {
            // 폰 이동에서는 파일이 명시될 수 있음
            fromFileHint = moveText[0]
        }
    }

    return Triple(pieceType, fromFileHint, fromRankHint)
}

// 기물의 출발 위치 찾기
internal fun findMoveSource(
    board: Array<CharArray>,
    isWhiteTurn: Boolean,
    pieceType: Char,
    toR: Int, toC: Int,
    fromFileHint: Char?,
    fromRankHint: Char?,
    enPassantTarget: String?
): Pair<Int, Int> {
    // 가능한 출발지 목록
    val candidates = if (pieceType == ChessPiece.PAWN) {
        findPawnSourceCandidates(board, isWhiteTurn, toR, toC, fromFileHint, enPassantTarget)
    } else {
        findPieceSourceCandidates(board, isWhiteTurn, pieceType, toR, toC, fromFileHint, fromRankHint)
    }

    if (candidates.isEmpty()) {
        throw IllegalArgumentException("유효하지 않은 이동: 출발지를 찾을 수 없습니다.")
    }

    // 여러 후보가 있을 경우 힌트를 사용해 필터링
    return candidates.first()
}

// 폰의 이동 출발지 후보 찾기
internal fun findPawnSourceCandidates(
    board: Array<CharArray>,
    isWhiteTurn: Boolean,
    toR: Int, toC: Int,
    fromFileHint: Char?,
    enPassantTarget: String?
): List<Pair<Int, Int>> {
    val candidates = mutableListOf<Pair<Int, Int>>()
    val direction = if (isWhiteTurn) -1 else 1 // 폰 이동 방향
    val pawnChar = if (isWhiteTurn) ChessPiece.PAWN else ChessPiece.PAWN.lowercaseChar()

    // 캡처 이동 (대각선) - 파일 힌트가 있는 경우
    if (fromFileHint != null) {
        val fromC = fileToIndex(fromFileHint)
        val fromR = toR - direction

        if (isInBoard(fromR, fromC) && board[fromR][fromC] == pawnChar) {
            candidates.add(Pair(fromR, fromC))
        }
    } else {
        // 일반 전진 이동
        // 한 칸 이동
        val fromR1 = toR - direction
        if (isInBoard(fromR1, toC) && board[fromR1][toC] == pawnChar) {
            candidates.add(Pair(fromR1, toC))
        }

        // 두 칸 이동 (시작 위치에서만)
        val startRank = if (isWhiteTurn) 6 else 1
        val fromR2 = toR - 2 * direction

        if (isInBoard(fromR2, toC) && fromR2 == startRank &&
            board[fromR2][toC] == pawnChar && board[fromR1][toC] == '.'
        ) {
            candidates.add(Pair(fromR2, toC))
        }
    }

    return candidates
}

// 다른 기물(폰 제외)의 이동 출발지 후보 찾기
internal fun findPieceSourceCandidates(
    board: Array<CharArray>,
    isWhiteTurn: Boolean,
    pieceType: Char,
    toR: Int, toC: Int,
    fromFileHint: Char?,
    fromRankHint: Char?
): List<Pair<Int, Int>> {
    val candidates = mutableListOf<Pair<Int, Int>>()
    val piece = if (isWhiteTurn) pieceType else pieceType.lowercaseChar()

    for (r in 0..7) {
        for (c in 0..7) {
            // 해당 위치에 필요한 기물이 있는지 확인
            if (board[r][c] != piece) continue

            // 힌트에 맞는지 확인
            if (fromFileHint != null && c != fileToIndex(fromFileHint)) continue
            if (fromRankHint != null && r != rankToIndex(fromRankHint)) continue

            // 기물 유형에 따른 이동 가능성 확인
            val canMove = when (pieceType) {
                ChessPiece.ROOK -> r == toR || c == toC  // 룩: 같은 행 또는 열
                ChessPiece.KNIGHT -> (abs(r - toR) == 1 && abs(c - toC) == 2) ||
                        (abs(r - toR) == 2 && abs(c - toC) == 1)  // 나이트: L자 이동
                ChessPiece.BISHOP -> abs(r - toR) == abs(c - toC)  // 비숍: 대각선
                ChessPiece.QUEEN -> r == toR || c == toC || abs(r - toR) == abs(c - toC)  // 퀸: 직선 또는 대각선
                ChessPiece.KING -> abs(r - toR) <= 1 && abs(c - toC) <= 1  // 킹: 주변 1칸
                else -> false
            }

            if (canMove) {
                // 경로상 다른 기물이 없는지 확인 (나이트 제외)
                if (pieceType == ChessPiece.KNIGHT || isPiecePathClear(board, r, c, toR, toC)) {
                    candidates.add(Pair(r, c))
                }
            }
        }
    }

    return candidates
}