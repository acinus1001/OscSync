package dev.kuro9.internal.chess.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChessComErrorObj(
    val code: Int,
    val message: String = "",
)