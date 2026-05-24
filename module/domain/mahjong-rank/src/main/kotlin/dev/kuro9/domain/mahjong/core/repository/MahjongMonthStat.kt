package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.domain.database.transformer.YearMonthStringTransformer
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MahjongMonthStats : LongIdTable("mahjong_month_stat") {
    val totalStat = reference("total_stat_id", MahjongTotalStats)

    val yearMonth = varchar("year_month", 6).transform(YearMonthStringTransformer)

    val rank = integer("rank")
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
        uniqueIndex(totalStat, yearMonth)
    }
}

class MahjongMonthStatEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MahjongMonthStatEntity>(MahjongMonthStats)

    var totalStat by MahjongMonthStatEntity referencedOn MahjongMonthStats.totalStat

    var yearMonth by MahjongMonthStats.yearMonth
    var rank by MahjongMonthStats.rank
    var totalUmaSum by MahjongMonthStats.totalUmaSum
    var totalGameCount by MahjongMonthStats.totalGameCount

    var firstPlaceCount by MahjongMonthStats.firstPlaceCount
    var secondPlaceCount by MahjongMonthStats.secondPlaceCount
    var thirdPlaceCount by MahjongMonthStats.thirdPlaceCount
    var fourthPlaceCount by MahjongMonthStats.fourthPlaceCount
    var tobiCount by MahjongMonthStats.tobiCount

    var firstPlaceRate by MahjongMonthStats.firstPlaceRate
    var secondPlaceRate by MahjongMonthStats.secondPlaceRate
    var thirdPlaceRate by MahjongMonthStats.thirdPlaceRate
    var fourthPlaceRate by MahjongMonthStats.fourthPlaceRate
    var tobiRate by MahjongMonthStats.tobiRate

    var avgPlace by MahjongMonthStats.avgPlace
    var avgUma by MahjongMonthStats.avgUma

    var createdAt by MahjongMonthStats.createdAt
    var updatedAt by MahjongMonthStats.updatedAt
}