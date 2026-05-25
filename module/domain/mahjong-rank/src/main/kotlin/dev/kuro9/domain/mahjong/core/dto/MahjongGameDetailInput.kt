package dev.kuro9.domain.mahjong.core.dto

import dev.kuro9.domain.mahjong.core.enums.MahjongSeki

data class MahjongGameDetailInput(
    val userId: Long,
    val score: Int,
    val seki: MahjongSeki?,
)