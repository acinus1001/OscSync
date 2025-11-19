package dev.kuro9.domain.chess.dto

import dev.kuro9.domain.chess.enums.EloType
import kotlinx.serialization.Serializable

@Serializable
data class ChessComGuildRank(
    val guildId: Long,
    val eloType: EloType,
    val rankList: List<UserInfo>, // 정렬된 상태
) : java.io.Serializable {

    @Serializable
    data class UserInfo(
        val userId: Long,
        val chessComUserName: String,
        val chessUserUrl: String,
        val guildRank: Int,
        val elo: Int,
    ) : java.io.Serializable
}
