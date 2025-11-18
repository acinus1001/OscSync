package dev.kuro9.domain.chess.exception

class ChessComUserNotRegisteredException(override val message: String = "등록된 사용자가 아닙니다.") : IllegalArgumentException()