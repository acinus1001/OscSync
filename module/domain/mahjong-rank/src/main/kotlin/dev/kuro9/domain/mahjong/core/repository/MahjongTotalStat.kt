package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MahjongTotalStats : LongIdTable("mahjong_total_stat") {
    val guildId = long("guild_id")
    val userId = long("user_id")

    val umaRank = integer("uma_rank")
    val gameCountRank = integer("game_count_rank")

    val totalUmaSum = decimal("total_uma_sum", 9, 1)
    val totalGameCount = integer("total_game_count")

    val firstPlaceCount = integer("first_place_count")
    val secondPlaceCount = integer("second_place_count")
    val thirdPlaceCount = integer("third_place_count")
    val fourthPlaceCount = integer("fourth_place_count")
    val tobiCount = integer("tobi_count")

    val firstPlaceRate = decimal("first_place_rate", 5, 2)
    val secondPlaceRate = decimal("second_place_rate", 5, 2)
    val thirdPlaceRate = decimal("third_place_rate", 5, 2)
    val fourthPlaceRate = decimal("fourth_place_rate", 5, 2)
    val tobiRate = decimal("tobi_rate", 5, 2)

    val avgPlace = decimal("avg_place", 3, 2)
    val avgUma = decimal("avg_uma", 9, 1)

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    init {
        uniqueIndex(userId, guildId)
    }
}

class MahjongTotalStatEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MahjongTotalStatEntity>(MahjongTotalStats)

    var guildId by MahjongTotalStats.guildId
    var userId by MahjongTotalStats.userId

    var umaRank by MahjongTotalStats.umaRank
    var gameCountRank by MahjongTotalStats.gameCountRank

    var totalUmaSum by MahjongTotalStats.totalUmaSum
    var totalGameCount by MahjongTotalStats.totalGameCount

    var firstPlaceCount by MahjongTotalStats.firstPlaceCount
    var secondPlaceCount by MahjongTotalStats.secondPlaceCount
    var thirdPlaceCount by MahjongTotalStats.thirdPlaceCount
    var fourthPlaceCount by MahjongTotalStats.fourthPlaceCount
    var tobiCount by MahjongTotalStats.tobiCount

    var firstPlaceRate by MahjongTotalStats.firstPlaceRate
    var secondPlaceRate by MahjongTotalStats.secondPlaceRate
    var thirdPlaceRate by MahjongTotalStats.thirdPlaceRate
    var fourthPlaceRate by MahjongTotalStats.fourthPlaceRate
    var tobiRate by MahjongTotalStats.tobiRate

    var avgPlace by MahjongTotalStats.avgPlace
    var avgUma by MahjongTotalStats.avgUma

    var createdAt by MahjongTotalStats.createdAt
    var updatedAt by MahjongTotalStats.updatedAt

    val monthStats by MahjongMonthStatEntity
        .referrersOn(MahjongMonthStats.totalStat)
        .orderBy(MahjongMonthStats.yearMonth to SortOrder.DESC)
}