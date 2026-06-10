package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.enums.MahjongSeki
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MahjongRecord(
    @ProtoNumber(1) val id: Long,
    @ProtoNumber(2) val guildId: Long,

    @ProtoNumber(3) val createdAt: LocalDateTime,
    @ProtoNumber(4) val createdBy: Long,
    @ProtoNumber(5) val createdByName: String,
    @ProtoNumber(6) val updatedAt: LocalDateTime,
    @ProtoNumber(7) val updatedBy: Long,
    @ProtoNumber(8) val updatedByName: String,
    @[ProtoNumber(9)] val deletedAt: LocalDateTime? = null,
    @ProtoNumber(10) val updatableUntil: LocalDateTime,

    @ProtoNumber(11) val userScores: List<UserScore>,

    @ProtoNumber(12) val scoreSettingId: Long,
) {
    /** [touUser] ~ [peiUser] 필드 사용 시 반드시 해당 bool값 확인 후 사용할 것 */
    val hasSekiData: Boolean get() = userScores.all { it.seki != null }

    val sekiOrders: List<UserScore>? get() = if (hasSekiData) userScores.sortedBy { it.seki } else null
    val touUser: UserScore get() = userScores.first { it.seki == MahjongSeki.TOU }
    val nanUser: UserScore get() = userScores.first { it.seki == MahjongSeki.NAN }
    val shaUser: UserScore get() = userScores.first { it.seki == MahjongSeki.SHA }
    val peiUser: UserScore get() = userScores.first { it.seki == MahjongSeki.PEI }

    val scoreOrders: List<UserScore> get() = userScores.sortedByDescending { it.score }
    val firstPlaceUser: UserScore get() = userScores.first { it.rank == 1 }
    val secondPlaceUser: UserScore get() = userScores.first { it.rank == 2 }
    val thirdPlaceUser: UserScore get() = userScores.first { it.rank == 3 }
    val fourthPlaceUser: UserScore get() = userScores.first { it.rank == 4 }

    @Serializable
    data class UserScore(
        @ProtoNumber(1) val userId: Long,
        @ProtoNumber(2) val rank: Int,
        @ProtoNumber(3) val score: Int,
        @ProtoNumber(4) val seki: MahjongSeki?, // 기존 데이터는 null, 마이그레이션 후 데이터는 non-null
        @ProtoNumber(5) val pointDeltaStringified: String,

        @ProtoNumber(6) val userName: String,
    )
}
