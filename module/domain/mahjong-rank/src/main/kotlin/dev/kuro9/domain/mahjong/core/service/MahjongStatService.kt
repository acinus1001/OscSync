package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.database.between
import dev.kuro9.domain.database.yearMonth
import dev.kuro9.domain.mahjong.core.annotation.MahjongInternalApi
import dev.kuro9.domain.mahjong.core.dto.MahjongGuildStat
import dev.kuro9.domain.mahjong.core.event.MahjongRankEvent
import dev.kuro9.domain.mahjong.core.repository.*
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.date.util.toRangeOfMonth
import io.github.harryjhin.slf4j.extension.info
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.math.BigDecimal
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@Service
@Transactional(readOnly = true)
class MahjongStatService {

    fun getUserStatOrNull(userId: Long, guildId: Long): MahjongTotalStatEntity? {
        return MahjongTotalStatEntity.find(
            (MahjongTotalStats.userId eq userId) and (MahjongTotalStats.guildId eq guildId)
        ).singleOrNull()
    }

    fun getGameCount(guildId: Long, ofGameId: Long? = null): Long {
        return MahjongGameEntity.find(((MahjongGames.guildId eq guildId).and(MahjongGames.deletedAt.isNull())).let {
            if (ofGameId == null) return@let it
            return@let it and (MahjongGames.id lessEq ofGameId)
        }).count()
    }

    fun getMonthGameCount(guildId: Long, yearMonth: YearMonth, ofGameId: Long? = null): Long {
        return MahjongGameEntity.find(
            (MahjongGames.guildId eq guildId)
                .and(MahjongGames.createdAt.between(yearMonth.toRangeOfMonth()))
                .and(MahjongGames.deletedAt.isNull()).let {
                    if (ofGameId == null) return@let it
                    return@let it and (MahjongGames.id lessEq ofGameId)
                }).count()
    }

    @Cacheable("mahjong-guild-cache", key = "#guildId")
    fun getServerStat(guildId: Long): MahjongGuildStat {
        info { "cache miss. calculating guild stat for guildId=$guildId" }
        val now = LocalDateTime.now()
        val nowMonth = now.date.yearMonth
        val (result, duration) = measureTimedValue {
            val totalGameCount = MahjongGameEntity.all().count()
            val gameCountPerMonthDescending =
                MahjongGames.select(MahjongGames.createdAt.yearMonth(), intLiteral(1).count())
                    .where { MahjongGames.guildId eq guildId }
                    .andWhere { MahjongGames.deletedAt.isNull() }
//                    .andWhere {
//                        MahjongGames.createdAt greaterEq nowMonth.minus(10, DateTimeUnit.MONTH).onDay(1)
//                            .atTime(LocalTime(0, 0))
//                    }
                    .groupBy(MahjongGames.createdAt.yearMonth())
                    .associate { resultRow ->
                        val yearMonth = YearMonth.parse(resultRow[MahjongGames.createdAt.yearMonth()])
                        val count = resultRow[intLiteral(1).count()]
                        yearMonth to count
                    }
                    .toSortedMap(reverseOrder())
            val highScoreGame = MahjongGames
                .innerJoin(MahjongGameResults)
                .selectAll()
                .where { MahjongGames.guildId eq guildId }
                .andWhere { MahjongGames.deletedAt.isNull() }
                .andWhere { MahjongGameResults.rank eq 1 }
                .orderBy(MahjongGameResults.score to SortOrder.DESC)
                .limit(1)
                .let { MahjongGameEntity.wrapRows(it).firstOrNull() }
            val lowScoreGame = MahjongGames
                .innerJoin(MahjongGameResults)
                .selectAll()
                .where { MahjongGames.guildId eq guildId }
                .andWhere { MahjongGames.deletedAt.isNull() }
                .andWhere { MahjongGameResults.rank eq 4 }
                .orderBy(MahjongGameResults.score to SortOrder.ASC)
                .limit(1)
                .let { MahjongGameEntity.wrapRows(it).firstOrNull() }

            MahjongGuildStat(
                guildId = guildId,
                totalGameCount = totalGameCount,
                gameCountPerMonthDescending = gameCountPerMonthDescending.toList(),
                highScore = highScoreGame?.results?.maxOf { it.score },
                highScoreGameId = highScoreGame?.id?.value,
                lowScore = lowScoreGame?.results?.minOf { it.score },
                lowScoreGameId = lowScoreGame?.id?.value,
            )
        }
        info { "guild stat calculated in $duration" }
        return result
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @[Async TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)]
    fun handleGameDataEvent(event: MahjongRankEvent) {
        info { "new rank event received" }

        when (event) {
            is MahjongRankEvent.NewGameResult -> event.userScoreList.forEach { gameResult ->
                calculateUserStat(
                    userId = gameResult.userId,
                    guildId = event.targetGuildId,
                    yearMonth = event.createdAt.date.yearMonth,
                )
            }

            is MahjongRankEvent.ModifyGameResult -> event.userScoreList.forEach { gameResult ->
                calculateUserStat(
                    userId = gameResult.userId,
                    guildId = event.targetGuildId,
                    yearMonth = event.createdAt.date.yearMonth,
                )
            }

            is MahjongRankEvent.DeleteGameResult -> event.gameUserIdSet.forEach { userId ->
                calculateUserStat(
                    userId = userId,
                    guildId = event.targetGuildId,
                    yearMonth = event.gameCreatedAt.date.yearMonth,
                )
            }
        }

        calculateRank(
            guildId = event.targetGuildId,
            yearMonth = event.createdAt.date.yearMonth,
        )

        info { "rank pre-calculation completed" }
    }

    fun getMonthPointRank(
        guildId: Long,
        n: Int,
        offset: Long,
        yearMonth: YearMonth,
    ): Pair<List<MahjongMonthStatEntity>, Long> {
        val data = MahjongMonthStats.innerJoin(MahjongTotalStats)
            .select(MahjongMonthStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .orderBy(MahjongMonthStats.umaRank to SortOrder.ASC)
            .limit(n)
            .offset(offset)
            .map {
                MahjongMonthStatEntity.wrapRow(it).load(MahjongMonthStatEntity::totalStat)
            }
        val count = MahjongMonthStats.innerJoin(MahjongTotalStats)
            .select(MahjongMonthStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .count()

        return data to count
    }

    fun getAllPointRank(
        guildId: Long,
        n: Int,
        offset: Long,
    ): Pair<List<MahjongTotalStatEntity>, Long> {
        val data = MahjongTotalStats.select(MahjongTotalStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .orderBy(MahjongTotalStats.umaRank to SortOrder.ASC)
            .limit(n)
            .offset(offset)
            .map { MahjongTotalStatEntity.wrapRow(it) }

        val count = MahjongTotalStats.select(MahjongTotalStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .count()

        return data to count
    }

    fun getMonthGameCountRank(
        guildId: Long,
        n: Int,
        offset: Long,
        yearMonth: YearMonth,
    ): Pair<List<MahjongMonthStatEntity>, Long> {
        val data = MahjongMonthStats.innerJoin(MahjongTotalStats)
            .select(MahjongMonthStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .orderBy(MahjongMonthStats.gameCountRank to SortOrder.ASC)
            .limit(n)
            .offset(offset)
            .map {
                MahjongMonthStatEntity.wrapRow(it).load(MahjongMonthStatEntity::totalStat)
            }

        val count = MahjongMonthStats.innerJoin(MahjongTotalStats)
            .select(MahjongMonthStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .count()

        return data to count
    }

    fun getAllGameCountRank(
        guildId: Long,
        n: Int,
        offset: Long,
    ): Pair<List<MahjongTotalStatEntity>, Long> {
        val data = MahjongTotalStats.select(MahjongTotalStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .orderBy(MahjongTotalStats.gameCountRank to SortOrder.ASC)
            .limit(n)
            .offset(offset)
            .map { MahjongTotalStatEntity.wrapRow(it) }

        val count = MahjongTotalStats.select(MahjongTotalStats.columns)
            .where { MahjongTotalStats.guildId eq guildId }
            .count()

        return data to count
    }

    // 유저 스탯 계산
    private fun calculateUserStat(userId: Long, guildId: Long, yearMonth: YearMonth): MahjongTotalStatEntity {
        val userGames = MahjongGameResults.innerJoin(MahjongGames).select(MahjongGameResults.columns)
            .where { MahjongGameResults.userId eq userId }
            .andWhere { MahjongGames.guildId eq guildId }
            .andWhere { MahjongGames.deletedAt.isNull() }
            .map { MahjongGameResultEntity.wrapRow(it) }
            .with(MahjongGameResultEntity::game, MahjongGameEntity::scoreSetting)

        // 1. 전체 스탯 계산
        val totalUmaSum = userGames.sumOf(MahjongGameResultEntity::point)
        val totalGameCount = userGames.count()

        val firstPlaceCount = userGames.count { g -> g.rank == 1 }
        val secondPlaceCount = userGames.count { g -> g.rank == 2 }
        val thirdPlaceCount = userGames.count { g -> g.rank == 3 }
        val fourthPlaceCount = userGames.count { g -> g.rank == 4 }
        val tobiCount = userGames.count { g -> g.score < 0 }

        val totalStat = MahjongTotalStats.upsertReturning(
            MahjongTotalStats.guildId, MahjongTotalStats.userId,
            onUpdateExclude = listOf(MahjongTotalStats.umaRank, MahjongTotalStats.gameCountRank),
            where = { (MahjongTotalStats.guildId eq guildId) and (MahjongTotalStats.userId eq userId) }
        ) {
            it[this.guildId] = guildId
            it[this.userId] = userId

            it[this.umaRank] = -1
            it[this.gameCountRank] = -1

            it[this.totalUmaSum] = totalUmaSum
            it[this.totalGameCount] = totalGameCount

            it[this.firstPlaceCount] = firstPlaceCount
            it[this.secondPlaceCount] = secondPlaceCount
            it[this.thirdPlaceCount] = thirdPlaceCount
            it[this.fourthPlaceCount] = fourthPlaceCount
            it[this.tobiCount] = tobiCount

            it[this.firstPlaceRate] = BigDecimal.valueOf(firstPlaceCount.toDouble() / totalGameCount * 100)
            it[this.secondPlaceRate] = BigDecimal.valueOf(secondPlaceCount.toDouble() / totalGameCount * 100)
            it[this.thirdPlaceRate] = BigDecimal.valueOf(thirdPlaceCount.toDouble() / totalGameCount * 100)
            it[this.fourthPlaceRate] = BigDecimal.valueOf(fourthPlaceCount.toDouble() / totalGameCount * 100)
            it[this.tobiRate] = BigDecimal.valueOf(tobiCount.toDouble() / totalGameCount * 100)

            it[this.avgPlace] =
                BigDecimal.valueOf(((1 * firstPlaceCount) + (2 * secondPlaceCount) + (3 * thirdPlaceCount) + (4 * fourthPlaceCount)).toDouble() / totalGameCount)
            it[this.avgUma] = BigDecimal.valueOf(totalUmaSum.toDouble() / totalGameCount)

            it[this.updatedAt] = LocalDateTime.now()
        }.single().let(MahjongTotalStatEntity::wrapRow)

        // 2. 월 스탯 계산
        val monthGames = userGames.filter { g -> g.game.createdAt.date.yearMonth == yearMonth }
        val monthTotalUmaSum = monthGames.sumOf(MahjongGameResultEntity::point)
        val monthTotalGameCount = monthGames.count()

        val monthFirstPlaceCount = monthGames.count { g -> g.rank == 1 }
        val monthSecondPlaceCount = monthGames.count { g -> g.rank == 2 }
        val monthThirdPlaceCount = monthGames.count { g -> g.rank == 3 }
        val monthFourthPlaceCount = monthGames.count { g -> g.rank == 4 }
        val monthTobiCount = monthGames.count { g -> g.score < 0 }

        MahjongMonthStats.upsert(
            MahjongMonthStats.totalStat, MahjongMonthStats.yearMonth,
            onUpdateExclude = listOf(MahjongMonthStats.umaRank, MahjongMonthStats.gameCountRank),
            where = { (MahjongMonthStats.totalStat eq totalStat.id) and (MahjongMonthStats.yearMonth eq yearMonth) }
        ) {
            it[this.totalStat] = totalStat.id
            it[this.yearMonth] = yearMonth

            it[this.umaRank] = -1
            it[this.gameCountRank] = -1

            it[this.totalUmaSum] = monthTotalUmaSum
            it[this.totalGameCount] = monthTotalGameCount

            it[this.firstPlaceCount] = monthFirstPlaceCount
            it[this.secondPlaceCount] = monthSecondPlaceCount
            it[this.thirdPlaceCount] = monthThirdPlaceCount
            it[this.fourthPlaceCount] = monthFourthPlaceCount
            it[this.tobiCount] = monthTobiCount

            it[this.firstPlaceRate] = BigDecimal.valueOf(monthFirstPlaceCount.toDouble() / monthTotalGameCount * 100)
            it[this.secondPlaceRate] = BigDecimal.valueOf(monthSecondPlaceCount.toDouble() / monthTotalGameCount * 100)
            it[this.thirdPlaceRate] = BigDecimal.valueOf(monthThirdPlaceCount.toDouble() / monthTotalGameCount * 100)
            it[this.fourthPlaceRate] = BigDecimal.valueOf(monthFourthPlaceCount.toDouble() / monthTotalGameCount * 100)
            it[this.tobiRate] = BigDecimal.valueOf(monthTobiCount.toDouble() / monthTotalGameCount * 100)

            it[this.avgPlace] =
                BigDecimal.valueOf(((1 * monthFirstPlaceCount) + (2 * monthSecondPlaceCount) + (3 * monthThirdPlaceCount) + (4 * monthFourthPlaceCount)).toDouble() / monthTotalGameCount)
            it[this.avgUma] = BigDecimal.valueOf(monthTotalUmaSum.toDouble() / monthTotalGameCount)

            it[this.updatedAt] = LocalDateTime.now()
        }

        return totalStat
    }

    // 해당 길드(서버)의 랭킹 재계산.
    private fun calculateRank(guildId: Long, yearMonth: YearMonth) {
        MahjongTotalStatEntity.find { (MahjongTotalStats.guildId eq guildId) }
            .orderBy(MahjongTotalStats.totalUmaSum to SortOrder.DESC)
            .forEachIndexed { i, entity -> entity.umaRank = i + 1 }

        MahjongTotalStatEntity.find { (MahjongTotalStats.guildId eq guildId) }
            .orderBy(MahjongTotalStats.gameCountRank to SortOrder.DESC)
            .forEachIndexed { i, entity -> entity.gameCountRank = i + 1 }

        MahjongMonthStats.innerJoin(MahjongTotalStats).select(MahjongMonthStats.columns)
            .where(MahjongTotalStats.guildId eq guildId)
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .orderBy(MahjongMonthStats.totalUmaSum to SortOrder.DESC)
            .map(MahjongMonthStatEntity::wrapRow)
            .forEachIndexed { i, entity -> entity.umaRank = i + 1 }

        MahjongMonthStats.innerJoin(MahjongTotalStats).select(MahjongMonthStats.columns)
            .where(MahjongTotalStats.guildId eq guildId)
            .andWhere { MahjongMonthStats.yearMonth eq yearMonth }
            .orderBy(MahjongMonthStats.gameCountRank to SortOrder.DESC)
            .map(MahjongMonthStatEntity::wrapRow)
            .forEachIndexed { i, entity -> entity.gameCountRank = i + 1 }
    }

    // 초기 통계 적재용 메소드
    @Transactional
    @MahjongInternalApi
    fun calculateInitial() {
        info { "initializing mahjong stat" }
        val guildIdList = MahjongGames.select(MahjongGames.guildId).withDistinct(true).map { it[MahjongGames.guildId] }
        info { "guild count: ${guildIdList.size}" }

        val time = measureTime {
            for (guildId in guildIdList) {
                info { "processing guildId: $guildId" }
                val userIdAndYearMonthList = MahjongGameResults.innerJoin(MahjongGames)
                    .select(MahjongGameResults.userId, MahjongGames.createdAt)
                    .where { MahjongGames.guildId eq guildId }
                    .map { it[MahjongGameResults.userId] to it[MahjongGames.createdAt].date.yearMonth }
                    .distinct()

                info { "data count: ${userIdAndYearMonthList.size}" }
                info { "processing calculateUserStat" }
                for ((userId, yearMonth) in userIdAndYearMonthList) {
                    calculateUserStat(userId, guildId, yearMonth)
                }
                info { "processing calculateRank" }
                for (yearMonth in userIdAndYearMonthList.map { it.second }.distinct()) {
                    calculateRank(guildId, yearMonth)
                }
            }
        }
        info { "job end. duration = $time" }
    }
}