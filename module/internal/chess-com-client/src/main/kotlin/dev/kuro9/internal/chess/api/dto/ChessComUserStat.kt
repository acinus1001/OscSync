package dev.kuro9.internal.chess.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChessComUserStat(
    @SerialName("chess_daily") val chessDaily: ChessGameStat? = ChessGameStat(),
    @SerialName("chess960_daily") val chess960Daily: ChessGameStat? = ChessGameStat(),
    @SerialName("chess_rapid") val chessRapid: ChessGameStat? = ChessGameStat(),
    @SerialName("chess_bullet") val chessBullet: ChessGameStat? = ChessGameStat(),
    @SerialName("chess_blitz") val chessBlitz: ChessGameStat? = ChessGameStat(),
    val fide: Int = 0,
    val tactics: TacticsStat? = null,
    @SerialName("puzzle_rush") val puzzleRush: PuzzleRushStat = PuzzleRushStat()
) {
    @Serializable
    data class ChessGameStat(
        val last: Stat? = null,
        val best: BestStat? = null,
        val record: Record? = null
    ) {
        @Serializable
        data class Stat(
            val rating: Int = 0,
            val date: Long = 0,
            val rd: Int = 0
        )

        @Serializable
        data class BestStat(
            val rating: Int = 0,
            val date: Long = 0,
            val game: String = ""
        )

        @Serializable
        data class Record(
            val win: Int = 0,
            val loss: Int = 0,
            val draw: Int = 0
        )
    }


    @Serializable
    data class TacticsStat(
        val highest: Info? = null,
        val lowest: Info? = null
    ) {
        @Serializable
        data class Info(
            val rating: Int = 0,
            val date: Long = 0
        )
    }

    @Serializable
    data class PuzzleRushStat(
        val daily: Info? = null,
        val best: Info? = null
    ) {
        @Serializable
        data class Info(
            val score: Int = 0,
            @SerialName("total_attempts") val totalAttempts: Int = 0
        )
    }

}
