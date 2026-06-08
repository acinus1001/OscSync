package dev.kuro9.application.homepage.mahjong.utils

import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.enums.MahjongSeki
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.of

fun MahjongGameEntity.toDto(getNameById: (Long) -> String): MahjongRecord = MahjongRecord(
    id = this.id.value,
    guildId = this.guildId,
    createdAt = this.createdAt,
    createdBy = this.createdBy,
    createdByName = getNameById(this.createdBy),
    updatedAt = this.updatedAt,
    updatedBy = this.updatedBy,
    updatedByName = getNameById(this.updatedBy),
    deletedAt = this.deletedAt,
    updatableUntil = this.updatableUntil,
    userScores = this.results.map {
        MahjongRecord.UserScore.of(
            userId = it.userId,
            rank = it.rank,
            score = it.score,
            seki = it.seki?.ordinal?.let(MahjongSeki.entries::getOrNull),
            pointDelta = it.point,
            userName = getNameById(it.userId),
        )
    },
    scoreSettingId = this.scoreSetting.id.value,
)