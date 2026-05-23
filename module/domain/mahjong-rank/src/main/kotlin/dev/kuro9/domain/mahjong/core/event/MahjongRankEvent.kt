package dev.kuro9.domain.mahjong.core.event

import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultModel
import kotlinx.datetime.LocalDateTime

sealed interface MahjongRankEvent {
    val targetGuildId: Long

    data class NewGameResult(
        override val targetGuildId: Long,
        val createdAt: LocalDateTime,
        val userScoreList: List<MahjongGameResultModel>,
    ) : MahjongRankEvent {
        val firstPlace = userScoreList[0]
        val secondPlace = userScoreList[1]
        val thirdPlace = userScoreList[2]
        val fourthPlace = userScoreList[3]
    }

    data class ModifyGameResult(
        override val targetGuildId: Long,
        val createdAt: LocalDateTime,
        val userScoreList: List<MahjongGameResultModel>,
        val modifiedDataSet: Set<MahjongGameResultModel>,
    ) : MahjongRankEvent {
        val firstPlace = userScoreList[0]
        val secondPlace = userScoreList[1]
        val thirdPlace = userScoreList[2]
        val fourthPlace = userScoreList[3]
    }
}