package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.enums.MahjongSeki
import java.math.BigDecimal

fun MahjongRecord.UserScore.Companion.of(
    userId: Long,
    userName: String,
    rank: Int,
    score: Int,
    seki: MahjongSeki?,
    pointDelta: BigDecimal,
): MahjongRecord.UserScore {
    return MahjongRecord.UserScore(
        userId = userId,
        rank = rank,
        score = score,
        seki = seki,
        pointDeltaStringified = pointDelta.toString(),
        userName = userName
    )
}