package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import kotlinx.serialization.Serializable

@Serializable
data class MahjongUserStat(
    val guildId: Long,
    val userId: Long,

    val umaRank: Int,
    val gameCountRank: Int,

    val totalUmaSumString: String,
    val totalGameCount: Int,

    val firstPlaceCount: Int,
    val secondPlaceCount: Int,
    val thirdPlaceCount: Int,
    val fourthPlaceCount: Int,
    val tobiCount: Int,

    val firstPlaceRateString: String,
    val secondPlaceRateString: String,
    val thirdPlaceRateString: String,
    val fourthPlaceRateString: String,
    val tobiRateString: String,

    val avgPlaceString: String,
    val avgUmaString: String,

    val graphImgUrl: String,
)