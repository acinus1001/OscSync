package dev.kuro9.application.homepage.mahjong.controller

import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.kuro9.domain.mahjong.core.repository.MahjongMonthStatEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongTotalStatEntity
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongGameCountRank
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPointRank
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@Transactional(readOnly = true)
@RestController
@RequestMapping("/services/mahjong/guilds/{guildId}/ranks")
class MahjongRankController(
    private val statService: MahjongStatService,
    private val rankService: MahjongRankService,
    private val discordSearchService: DiscordSearchService,
) {

    @GetMapping("/all/point")
    fun getPointRank(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @RequestParam(required = false) page: Int = 1,
    ): MahjongPagingResult<MahjongPointRank> {
        require(page >= 1)
        val (pageSize, offset) = 50 to ((page - 1L) * 50)
        val (data: List<MahjongTotalStatEntity>, count: Long) = statService.getAllPointRank(
            guildId = guildId,
            n = pageSize,
            offset = offset
        )
        val entityCount = rankService.getGuildGamesAtRange(
            guildId = guildId,
            start = null,
            endInclusive = null,
        ).count().toInt()

        return MahjongPagingResult(
            page = page,
            maxPage = (entityCount + pageSize - 1) / pageSize,
            totalElementCount = entityCount,
            content = data.mapIndexed { i, entity ->
                MahjongPointRank(
                    rank = i + 1 + ((page - 1) * pageSize),
                    userId = entity.userId,
                    userName = discordSearchService.findById(entity.userId)?.name ?: "<unknown>",
                    statUpdatedAt = entity.updatedAt.toInstant(UtcOffset(9)),
                    pointString = "%+,.1f".format(entity.totalUmaSum),
                )
            }
        )
    }

    @GetMapping("/all/game-count")
    fun getGameCountRank(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @RequestParam(required = false) page: Int = 1,
    ): MahjongPagingResult<MahjongGameCountRank> {
        require(page >= 1)
        val (pageSize, offset) = 50 to ((page - 1L) * 50)
        val (data: List<MahjongTotalStatEntity>, count: Long) = statService.getAllGameCountRank(
            guildId = guildId,
            n = pageSize,
            offset = offset
        )
        val entityCount = rankService.getGuildGamesAtRange(
            guildId = guildId,
            start = null,
            endInclusive = null,
        ).count().toInt()

        return MahjongPagingResult(
            page = page,
            maxPage = (entityCount + pageSize - 1) / pageSize,
            totalElementCount = entityCount,
            content = data.mapIndexed { i, entity ->
                MahjongGameCountRank(
                    rank = i + 1 + ((page - 1) * pageSize),
                    userId = entity.userId,
                    userName = discordSearchService.findById(entity.userId)?.name ?: "<unknown>",
                    statUpdatedAt = entity.updatedAt.toInstant(UtcOffset(9)),
                    gameCount = entity.totalGameCount,
                )
            }
        )
    }

    @GetMapping("/{yearMonth}/point")
    fun getPointRankByYearMonth(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @PathVariable yearMonth: String,
        @RequestParam(required = false) page: Int = 1,
    ): MahjongPagingResult<MahjongPointRank> {
        require(page >= 1)
        val (pageSize, offset) = 50 to ((page - 1L) * 50)
        val yearMonthValue = YearMonth.parse(yearMonth)
        val (data: List<MahjongMonthStatEntity>, count: Long) = statService.getMonthPointRank(
            guildId = guildId,
            n = pageSize,
            offset = offset,
            yearMonth = yearMonthValue,
        )
        val entityCount = rankService.getGuildGamesAtRange(
            guildId = guildId,
            start = yearMonthValue.firstDay,
            endInclusive = yearMonthValue.lastDay,
        ).count().toInt()

        return MahjongPagingResult(
            page = page,
            maxPage = (entityCount + pageSize - 1) / pageSize,
            totalElementCount = entityCount,
            content = data.mapIndexed { i, entity ->
                MahjongPointRank(
                    rank = i + 1 + ((page - 1) * pageSize),
                    userId = entity.totalStat.userId,
                    userName = discordSearchService.findById(entity.totalStat.userId)?.name ?: "<unknown>",
                    statUpdatedAt = entity.updatedAt.toInstant(UtcOffset(9)),
                    pointString = "%+,.1f".format(entity.totalUmaSum),
                )
            }
        )
    }

    @GetMapping("/{yearMonth}/game-count")
    fun getGameCountRankByYearMonth(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @PathVariable yearMonth: String,
        @RequestParam(required = false) page: Int = 1,
    ): MahjongPagingResult<MahjongGameCountRank> {
        require(page >= 1)
        val (pageSize, offset) = 50 to ((page - 1L) * 50)
        val yearMonthValue = YearMonth.parse(yearMonth)
        val (data: List<MahjongMonthStatEntity>, count: Long) = statService.getMonthGameCountRank(
            guildId = guildId,
            n = pageSize,
            offset = offset,
            yearMonth = yearMonthValue,
        )
        val entityCount = rankService.getGuildGamesAtRange(
            guildId = guildId,
            start = yearMonthValue.firstDay,
            endInclusive = yearMonthValue.lastDay,
        ).count().toInt()

        return MahjongPagingResult(
            page = page,
            maxPage = (entityCount + pageSize - 1) / pageSize,
            totalElementCount = entityCount,
            content = data.mapIndexed { i, entity ->
                MahjongGameCountRank(
                    rank = i + 1 + ((page - 1) * pageSize),
                    userId = entity.totalStat.userId,
                    userName = discordSearchService.findById(entity.totalStat.userId)?.name ?: "<unknown>",
                    statUpdatedAt = entity.updatedAt.toInstant(UtcOffset(9)),
                    gameCount = entity.totalGameCount,
                )
            }
        )
    }
}