package dev.kuro9.application.homepage.mahjong.controller

import dev.kuro9.application.homepage.mahjong.utils.toDto
import dev.kuro9.domain.mahjong.core.repository.MahjongGames
import dev.kuro9.domain.mahjong.core.repository.MahjongMonthStatEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongTotalStatEntity
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import dev.kuro9.domain.mahjong.image.service.MahjongScoreGraphService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongGuildStat
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat
import kotlinx.datetime.YearMonth
import org.jetbrains.exposed.v1.core.SortOrder
import org.springframework.http.HttpStatusCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import kotlin.io.encoding.Base64
import dev.kuro9.domain.mahjong.core.dto.MahjongGuildStat as MahjongGuildStatDto

@Transactional(readOnly = true)
@RestController
@RequestMapping("/services/mahjong/guilds/{guildId}/stats")
class MahjongStatController(
    private val statService: MahjongStatService,
    private val scoreGraphService: MahjongScoreGraphService,
    private val rankService: MahjongRankService,
) {

    @GetMapping
    fun getGuildStat(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
    ): MahjongGuildStat {
        val stat: MahjongGuildStatDto = statService.getServerStat(guildId)
        return MahjongGuildStat(
            guildId = stat.guildId,
            totalGameCount = stat.totalGameCount,
            gameCountPerMonthDescending = stat.gameCountPerMonthDescending,
            highScore = stat.highScore,
            highScoreGameId = stat.highScoreGameId,
            lowScore = stat.lowScore,
            lowScoreGameId = stat.lowScoreGameId,
        )
    }

    @GetMapping("/{userId}")
    fun getUserStat(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @PathVariable userId: Long,
    ): MahjongUserStat {
        val stat: MahjongTotalStatEntity = statService.getUserStatOrNull(userId, guildId)
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(404))
        val recentData = rankService
            .getUserGamesAtRange(
                userId = userId,
                guildId = guildId,
            )
            .orderBy(MahjongGames.id to SortOrder.DESC)
            .limit(10)
            .reversed()
            .map { game ->
                val userResult = game.results.first { it.userId == userId }
                userResult.rank to (userResult.score >= 50_000)
            }
        val image = scoreGraphService.scoreGraphGen(recentData)
        val imageUrl = run {
            val header = "data:image/png;base64,"
            val imageEncoded = Base64.encode(image.readBytes())
            return@run "$header$imageEncoded"
        }
        return stat.toDto(imageUrl)
    }

    @GetMapping("/{userId}/yearmonth/{yearMonth}")
    fun getUserStatByYearMonth(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @PathVariable userId: Long,
        @PathVariable yearMonth: String,
    ): MahjongUserStat {
        val yearMonthValue = YearMonth.parse(yearMonth)
        val stat: MahjongMonthStatEntity = statService.getUserStatOrNull(userId, guildId)
            ?.monthStats
            ?.firstOrNull { it.yearMonth == yearMonthValue }
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(404))
        val recentData = rankService
            .getUserGamesAtRange(
                userId = userId,
                guildId = guildId,
                start = yearMonthValue.firstDay,
                endInclusive = yearMonthValue.lastDay,
            )
            .orderBy(MahjongGames.id to SortOrder.DESC)
            .limit(10)
            .reversed()
            .map { game ->
                val userResult = game.results.first { it.userId == userId }
                userResult.rank to (userResult.score >= 50_000)
            }
        val image = scoreGraphService.scoreGraphGen(recentData)
        val imageUrl = run {
            val header = "data:image/png;base64,"
            val imageEncoded = Base64.encode(image.readBytes())
            return@run "$header$imageEncoded"
        }
        return stat.toDto(imageUrl)
    }
}