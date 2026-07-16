package dev.kuro9.application.homepage.mahjong.utils

import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongMonthStatEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongTotalStatEntity
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongDetailRecord
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.enums.MahjongSeki
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.of
import kotlin.io.encoding.Base64

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

fun MahjongGameEntity.toDetailedDto(getNameById: (Long) -> String): MahjongDetailRecord = MahjongDetailRecord(
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
        MahjongDetailRecord.UserScore.of(
            userId = it.userId,
            rank = it.rank,
            score = it.score,
            seki = it.seki?.ordinal?.let(MahjongSeki.entries::getOrNull),
            pointDelta = it.point,
            userName = getNameById(it.userId),
        )
    },
    scoreSettingInfo = MahjongDetailRecord.ScoreSettingInfo(
        id = this.scoreSetting.id.value,
        umaSetting = listOf(
            this.scoreSetting.umaFirst,
            this.scoreSetting.umaSecond,
            this.scoreSetting.umaThird,
            this.scoreSetting.umaFourth
        ),
        startScore = this.scoreSetting.startScore,
        returnScore = this.scoreSetting.returnScore,
        createdAt = this.scoreSetting.createdAt,
        createdBy = this.scoreSetting.createdBy,
        createdByName = getNameById(this.scoreSetting.createdBy),
    ),
    imageBase64Url = run {
        val format = this.imageMime ?: return@run null
        val header = "data:$format;base64,"
        val imageEncoded = Base64.encode(this.image?.bytes ?: return@run null)
        return@run "$header$imageEncoded"
    },
    modifyLogs = this.editLogs.map { log ->
        MahjongDetailRecord.ModifyLog(
            id = log.id.value,
            type = MahjongDetailRecord.ModifyLog.LogType.entries[log.type.ordinal],
            originalTouUserId = log.originalTouUserId,
            originalNanUserId = log.originalNanUserId,
            originalShaUserId = log.originalShaUserId,
            originalPeiUserId = log.originalPeiUserId,
            originalTouUserScore = log.originalTouUserScore,
            originalNanUserScore = log.originalNanUserScore,
            originalShaUserScore = log.originalShaUserScore,
            originalPeiUserScore = log.originalPeiUserScore,
            newTouUserId = log.newTouUserId,
            newNanUserId = log.newNanUserId,
            newShaUserId = log.newShaUserId,
            newPeiUserId = log.newPeiUserId,
            newTouUserScore = log.newTouUserScore,
            newNanUserScore = log.newNanUserScore,
            newShaUserScore = log.newShaUserScore,
            newPeiUserScore = log.newPeiUserScore,
            createdAt = log.createdAt,
            createdBy = log.createdBy,
            createdByName = getNameById(log.createdBy)
        )
    }
)

fun MahjongTotalStatEntity.toDto(imageUrl: String) = MahjongUserStat(
    guildId = this.guildId,
    userId = this.userId,
    umaRank = this.umaRank,
    gameCountRank = this.gameCountRank,
    totalUmaSumString = "%,.1f".format(this.totalUmaSum),
    totalGameCount = this.totalGameCount,
    firstPlaceCount = this.firstPlaceCount,
    secondPlaceCount = this.secondPlaceCount,
    thirdPlaceCount = this.thirdPlaceCount,
    fourthPlaceCount = this.fourthPlaceCount,
    tobiCount = this.tobiCount,
    firstPlaceRateString = "%.2f".format(this.firstPlaceRate),
    secondPlaceRateString = "%.2f".format(this.secondPlaceRate),
    thirdPlaceRateString = "%.2f".format(this.thirdPlaceRate),
    fourthPlaceRateString = "%.2f".format(this.fourthPlaceRate),
    tobiRateString = "%.2f".format(this.tobiRate),
    avgPlaceString = "%.2f".format(this.avgPlace),
    avgUmaString = "%+.1f".format(this.avgUma),
    graphImgUrl = imageUrl,
)

fun MahjongMonthStatEntity.toDto(imageUrl: String) = MahjongUserStat(
    guildId = this.totalStat.guildId,
    userId = this.totalStat.userId,
    umaRank = this.umaRank,
    gameCountRank = this.gameCountRank,
    totalUmaSumString = "%,.1f".format(this.totalUmaSum),
    totalGameCount = this.totalGameCount,
    firstPlaceCount = this.firstPlaceCount,
    secondPlaceCount = this.secondPlaceCount,
    thirdPlaceCount = this.thirdPlaceCount,
    fourthPlaceCount = this.fourthPlaceCount,
    tobiCount = this.tobiCount,
    firstPlaceRateString = "%.2f".format(this.firstPlaceRate),
    secondPlaceRateString = "%.2f".format(this.secondPlaceRate),
    thirdPlaceRateString = "%.2f".format(this.thirdPlaceRate),
    fourthPlaceRateString = "%.2f".format(this.fourthPlaceRate),
    tobiRateString = "%.2f".format(this.tobiRate),
    avgPlaceString = "%.2f".format(this.avgPlace),
    avgUmaString = "%+.1f".format(this.avgUma),
    graphImgUrl = imageUrl,
)