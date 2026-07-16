@file:OptIn(ExperimentalSerializationApi::class)

package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import kotlinx.datetime.YearMonth
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class MahjongGuildStat(
    @ProtoNumber(1) val guildId: Long,
    @ProtoNumber(2) val totalGameCount: Long,
    @ProtoNumber(3) val gameCountPerMonthDescending: List<Pair<YearMonth, Long>>,
    @ProtoNumber(4) val highScore: Int? = null,
    @ProtoNumber(5) val highScoreGameId: Long? = null,
    @ProtoNumber(6) val lowScore: Int? = null,
    @ProtoNumber(7) val lowScoreGameId: Long? = null,
)
