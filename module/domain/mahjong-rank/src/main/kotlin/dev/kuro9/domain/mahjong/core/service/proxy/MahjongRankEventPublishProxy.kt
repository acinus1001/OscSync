package dev.kuro9.domain.mahjong.core.service.proxy

import dev.kuro9.domain.mahjong.core.dto.MahjongGameDeleteInfo
import dev.kuro9.domain.mahjong.core.event.MahjongRankEvent
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultEntity
import dev.kuro9.domain.mahjong.core.repository.toModel
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@[Aspect Transactional(readOnly = true) Service]
class MahjongRankEventPublishProxy(
    private val eventPublisher: ApplicationEventPublisher,
) {

    @AfterReturning(
        pointcut = "execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.save(..))",
        returning = "game",
    )
    fun publishSave(game: MahjongGameEntity) {
        eventPublisher.publishEvent(
            MahjongRankEvent.NewGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = game.results.map { it.toModel() }.sorted()
            )
        )
    }

    @AfterReturning(
        pointcut = "execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.modify(..))",
        returning = "modifiedResult",
    )
    fun publishModify(modifiedResult: Pair<MahjongGameEntity, List<MahjongGameResultEntity>>) {
        val (game, results) = modifiedResult
        eventPublisher.publishEvent(
            MahjongRankEvent.ModifyGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = results.map { it.toModel() }.sorted(),
            )
        )
    }

    @AfterReturning(
        pointcut = "execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.delete(..))",
        returning = "deletedInfo"
    )
    fun publishDelete(deletedInfo: MahjongGameDeleteInfo) {
        eventPublisher.publishEvent(
            MahjongRankEvent.DeleteGameResult(
                targetGuildId = deletedInfo.guildId,
                createdAt = LocalDateTime.now(),
                gameId = deletedInfo.gameId,
                gameUserIdSet = deletedInfo.gameUserIdSet,
                gameCreatedAt = deletedInfo.gameCreatedAt,
            )
        )
    }
}