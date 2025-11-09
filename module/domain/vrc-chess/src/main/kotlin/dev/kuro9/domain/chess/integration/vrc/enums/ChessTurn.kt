package dev.kuro9.domain.chess.integration.vrc.enums

enum class ChessTurn {
    WHITE, BLACK;

    fun ofReverse(): ChessTurn = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
    }
}