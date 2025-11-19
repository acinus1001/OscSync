package dev.kuro9.internal.chess.api.exception

class ChessApiFailureException(
    val httpStatus: Int,
    val code: Int,
    val apiMessage: String,
) : RuntimeException("Chess.com API Failure : code=$code, message=$apiMessage")