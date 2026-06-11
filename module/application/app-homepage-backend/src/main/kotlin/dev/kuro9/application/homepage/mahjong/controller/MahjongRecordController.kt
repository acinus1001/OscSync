package dev.kuro9.application.homepage.mahjong.controller

import dev.kuro9.application.homepage.mahjong.utils.toDetailedDto
import dev.kuro9.application.homepage.mahjong.utils.toDto
import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.kuro9.domain.mahjong.core.repository.MahjongGames
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongDetailRecord
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.SortOrder
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Transactional(readOnly = true)
@RestController
@RequestMapping("/services/mahjong/guilds/{guildId}/records")
class MahjongRecordController(
    private val statService: MahjongStatService,
    private val rankService: MahjongRankService,
    private val discordSearchService: DiscordSearchService,
) {

    @GetMapping(produces = [MediaType.APPLICATION_PROTOBUF_VALUE])
    fun getAllRecords(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @RequestParam(required = false) start: LocalDate? = null,
        @RequestParam(required = false) endInclusive: LocalDate? = null,
        @RequestParam(required = false) userId: Long? = null,
        @RequestParam page: Int, // one-based
        @PathVariable guildId: Long,
    ): MahjongPagingResult<MahjongRecord> {
        require(page >= 1) { "페이지는 1보다 크거나 같아야 합니다." }

        val size = 30
        val searchResult = when {
            userId != null -> rankService.getUserGamesAtRange(
                userId = userId,
                guildId = guildId,
                start = start,
                endInclusive = endInclusive,
            )

            else -> rankService.getGuildGamesAtRange(
                guildId = guildId,
                start = start,
                endInclusive = endInclusive,
            )
        }
        val totalCount = searchResult.count().toInt()

        val searchResultList: List<MahjongRecord> = searchResult
            .orderBy(MahjongGames.id to SortOrder.DESC)
            .limit(size)
            .offset((page - 1L) * size)
            .toList()
            .map { it.toDto { userId -> discordSearchService.findById(userId)?.name ?: "<unknown:$userId>" } }

        return MahjongPagingResult(
            page = page,
            maxPage = totalCount / size + if (totalCount % size == 0) 0 else 1,
            totalElementCount = totalCount,
            content = searchResultList,
        )
    }

    @GetMapping("/{recordId}", produces = [MediaType.APPLICATION_PROTOBUF_VALUE])
    fun getRecord(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable guildId: Long,
        @PathVariable recordId: Long,
    ): MahjongDetailRecord {
        val gameInfo = rankService.getGameById(recordId)?.takeIf { it.guildId == guildId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return gameInfo.toDetailedDto { userId -> discordSearchService.findById(userId)?.name ?: "<unknown:$userId>" }
    }
}