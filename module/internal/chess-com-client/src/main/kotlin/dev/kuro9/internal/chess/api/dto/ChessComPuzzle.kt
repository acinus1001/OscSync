package dev.kuro9.internal.chess.api.dto

import dev.kuro9.multiplatform.common.serialization.serializer.instant.UnixTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
@OptIn(ExperimentalTime::class)
data class ChessComPuzzle(
    val title: String,
    val url: String,
    @SerialName("publish_time") val publishTime: UnixTimestamp,
    val fen: String,
    val pgn: String,
    val image: String,
) {
}