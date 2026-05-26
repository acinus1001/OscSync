package dev.kuro9.domain.mahjong.core.dto

import kotlinx.datetime.LocalDateTime

data class MahjongGameDeleteInfo(
    val guildId: Long,
    val gameId: Long,
    val gameUserIdSet: Set<Long>,
    val gameCreatedAt: LocalDateTime,
)
