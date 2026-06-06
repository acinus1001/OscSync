package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MahjongScoreSettings : LongIdTable("mahjong_score_setting") {
    val guildId = long("guild_id")

    // startScore != returnScore 일 때 오카 아리
    val startScore = integer("start_score").default(25000)
    val returnScore = integer("return_score").default(25000)

    val umaFirst = integer("uma_first").default(15)
    val umaSecond = integer("uma_second").default(5)
    val umaThird = integer("uma_third").default(-5)
    val umaFourth = integer("uma_fourth").default(-15)

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val createdBy = long("created_by")
}

class MahjongScoreSettingEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MahjongScoreSettingEntity>(MahjongScoreSettings)

    var guildId by MahjongScoreSettings.guildId

    var startScore by MahjongScoreSettings.startScore
    var returnScore by MahjongScoreSettings.returnScore

    var umaFirst by MahjongScoreSettings.umaFirst
    var umaSecond by MahjongScoreSettings.umaSecond
    var umaThird by MahjongScoreSettings.umaThird
    var umaFourth by MahjongScoreSettings.umaFourth

    var createdAt by MahjongScoreSettings.createdAt
    var createdBy by MahjongScoreSettings.createdBy

    /* calculated fields */
    val hasOka: Boolean get() = startScore != returnScore
    val okaAmount: Int get() = (returnScore - startScore) * 4
    fun getUma(rank: Int): Int = when (rank) {
        1 -> umaFirst
        2 -> umaSecond
        3 -> umaThird
        4 -> umaFourth
        else -> throw IllegalArgumentException("Invalid rank: $rank")
    }

    fun getOka(rank: Int): Int = if (rank == 1) okaAmount else 0
}