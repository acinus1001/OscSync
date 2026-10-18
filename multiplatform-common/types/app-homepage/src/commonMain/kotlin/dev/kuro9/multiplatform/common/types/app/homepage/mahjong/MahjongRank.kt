package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import kotlinx.serialization.Serializable
import kotlin.time.Instant

interface MahjongRank {
    val rank: Int // 1-index
    val userId: Long
    val userName: String
    val statUpdatedAt: Instant
}

@Serializable
data class MahjongPointRank(
    override val rank: Int,
    override val userId: Long,
    override val userName: String,
    override val statUpdatedAt: Instant,
    val pointString: String, // + 기호, 소수점 한자리까지 포함된 숫자 string (소수점 정확도 문제 피하기 위해 문자열로..)
) : MahjongRank

@Serializable
data class MahjongGameCountRank(
    override val rank: Int,
    override val userId: Long,
    override val userName: String,
    override val statUpdatedAt: Instant,
    val gameCount: Int,
) : MahjongRank