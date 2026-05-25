package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.mahjong.core.repository.MahjongScoreSettingEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongScoreSettings
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MahjongScoreSettingService {

    fun getLatestScoreSetting(guildId: Long): MahjongScoreSettingEntity {
        return MahjongScoreSettingEntity.find(MahjongScoreSettings.guildId eq guildId)
            .orderBy(MahjongScoreSettings.id to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?: MahjongScoreSettingEntity.new { // 작혼 기본
                this.guildId = guildId
                this.createdBy = 400579163959853056L
            }
    }

    fun getAllScoreSetting(guildId: Long): List<MahjongScoreSettingEntity> {
        return MahjongScoreSettingEntity.find(MahjongScoreSettings.guildId eq guildId)
            .orderBy(MahjongScoreSettings.id to SortOrder.DESC)
            .toList()
    }

    fun postNewScoreSetting(
        guildId: Long,
        userId: Long,

        startScore: Int,
        returnScore: Int,

        umaFirst: Int,
        umaSecond: Int,
        umaThird: Int,
        umaFourth: Int,
    ): MahjongScoreSettingEntity {
        val latestSetting = MahjongScoreSettingEntity.find(MahjongScoreSettings.guildId eq guildId)
            .orderBy(MahjongScoreSettings.id to SortOrder.DESC)
            .limit(1)
            .singleOrNull()

        if (latestSetting != null) {
            // 바로 이전 세팅과 동일한 세팅인지 체크
            if (latestSetting.startScore == startScore &&
                latestSetting.returnScore == returnScore &&
                latestSetting.umaFirst == umaFirst &&
                latestSetting.umaSecond == umaSecond &&
                latestSetting.umaThird == umaThird &&
                latestSetting.umaFourth == umaFourth
            ) return latestSetting
        }

        return MahjongScoreSettingEntity.new {
            this.guildId = guildId
            this.startScore = startScore
            this.returnScore = returnScore
            this.umaFirst = umaFirst
            this.umaSecond = umaSecond
            this.umaThird = umaThird
            this.umaFourth = umaFourth
            this.createdBy = userId
        }
    }
}