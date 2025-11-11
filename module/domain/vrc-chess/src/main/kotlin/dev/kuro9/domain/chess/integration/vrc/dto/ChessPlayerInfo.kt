package dev.kuro9.domain.chess.integration.vrc.dto

sealed interface ChessPlayerInfo {
    val ipHash: Int

    @JvmInline
    value class User(override val ipHash: Int) : ChessPlayerInfo {

        companion object {
            fun fromIp(ip: String): User = User(ip.hashCode())
        }
    }

    data object Bot : ChessPlayerInfo {
        override val ipHash: Int = "BOT".hashCode()
    }

    companion object {
        fun of(hash: Int): ChessPlayerInfo {
            return if (hash == Bot.ipHash) Bot else User(hash)
        }
    }
}