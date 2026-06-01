package dev.kuro9.domain.mahjong.core.service.proxy

import dev.kuro9.domain.mahjong.core.dto.MahjongGameDetailInput
import dev.kuro9.domain.mahjong.core.enums.MahjongLogType
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEditLogEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultEntity
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@[Aspect Transactional Service]
class MahjongRankModifyLogProxy {

    @Around("execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.save(..)) && args(createdUserId, createdGuildId, imageUrl, imageMediaType, userScoreList)")
    fun logSave(
        createdUserId: Long,
        createdGuildId: Long,
        imageUrl: String? = null,
        imageMediaType: MediaType? = MediaType.APPLICATION_OCTET_STREAM,
        vararg userScoreList: MahjongGameDetailInput,
        jointPoint: ProceedingJoinPoint,
    ) {
        val result = jointPoint.proceed() as MahjongGameEntity
        MahjongGameEditLogEntity.new {
            game = result.id
            type = MahjongLogType.NEW

            originalTouUserId = null
            originalNanUserId = null
            originalShaUserId = null
            originalPeiUserId = null
            originalTouUserScore = null
            originalNanUserScore = null
            originalShaUserScore = null
            originalPeiUserScore = null

            userScoreList.first { u -> u.seki == MahjongSeki.TOU }.let { tou ->
                newTouUserId = tou.userId
                newTouUserScore = tou.score
            }
            userScoreList.first { u -> u.seki == MahjongSeki.NAN }.let { nan ->
                newNanUserId = nan.userId
                newNanUserScore = nan.score
            }
            userScoreList.first { u -> u.seki == MahjongSeki.SHA }.let { sha ->
                newShaUserId = sha.userId
                newShaUserScore = sha.score
            }
            userScoreList.first { u -> u.seki == MahjongSeki.PEI }.let { pei ->
                newPeiUserId = pei.userId
                newPeiUserScore = pei.score
            }

            createdAt = LocalDateTime.now()
            createdBy = createdUserId
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Around("execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.modify(..)) && args(id, modifyUserId, modifyData)")
    fun logModify(
        id: Long,
        modifyUserId: Long,
        vararg modifyData: MahjongGameDetailInput,
        jointPoint: ProceedingJoinPoint,
    ) {
        val originalGameResults = MahjongGameEntity.findById(id)?.results?.toList() ?: run {
            // case of game not found
            jointPoint.proceed()
            return
        }

        val (gameInfo, results) = jointPoint.proceed() as Pair<MahjongGameEntity, List<MahjongGameResultEntity>>

        MahjongGameEditLogEntity.new {
            game = gameInfo.id
            type = MahjongLogType.MODIFY

            originalGameResults.first { u -> u.seki == MahjongSeki.TOU }.let { tou ->
                originalTouUserId = tou.userId
                originalTouUserScore = tou.score
            }
            originalGameResults.first { u -> u.seki == MahjongSeki.NAN }.let { nan ->
                originalNanUserId = nan.userId
                originalNanUserScore = nan.score
            }
            originalGameResults.first { u -> u.seki == MahjongSeki.SHA }.let { sha ->
                originalShaUserId = sha.userId
                originalShaUserScore = sha.score
            }
            originalGameResults.first { u -> u.seki == MahjongSeki.PEI }.let { pei ->
                originalPeiUserId = pei.userId
                originalPeiUserScore = pei.score
            }

            results.first { u -> u.seki == MahjongSeki.TOU }.let { tou ->
                newTouUserId = tou.userId
                newTouUserScore = tou.score
            }
            results.first { u -> u.seki == MahjongSeki.NAN }.let { nan ->
                newNanUserId = nan.userId
                newNanUserScore = nan.score
            }
            results.first { u -> u.seki == MahjongSeki.SHA }.let { sha ->
                newShaUserId = sha.userId
                newShaUserScore = sha.score
            }
            results.first { u -> u.seki == MahjongSeki.PEI }.let { pei ->
                newPeiUserId = pei.userId
                newPeiUserScore = pei.score
            }

            createdAt = LocalDateTime.now()
            createdBy = modifyUserId
        }
    }

    @Before("execution(* dev.kuro9.domain.mahjong.core.service.MahjongRankService.delete(..)) && args(id, modifyUserId)")
    fun logDelete(id: Long, modifyUserId: Long) {
        val (gameInfo, results) = MahjongGameEntity.findById(id)?.let { it to it.results.toList() } ?: return

        MahjongGameEditLogEntity.new {
            game = gameInfo.id
            type = MahjongLogType.DELETE

            results.first { u -> u.seki == MahjongSeki.TOU }.let { tou ->
                originalTouUserId = tou.userId
                originalTouUserScore = tou.score
            }
            results.first { u -> u.seki == MahjongSeki.NAN }.let { nan ->
                originalNanUserId = nan.userId
                originalNanUserScore = nan.score
            }
            results.first { u -> u.seki == MahjongSeki.SHA }.let { sha ->
                originalShaUserId = sha.userId
                originalShaUserScore = sha.score
            }
            results.first { u -> u.seki == MahjongSeki.PEI }.let { pei ->
                originalPeiUserId = pei.userId
                originalPeiUserScore = pei.score
            }

            newTouUserId = null
            newNanUserId = null
            newShaUserId = null
            newPeiUserId = null
            newTouUserScore = null
            newNanUserScore = null
            newShaUserScore = null
            newPeiUserScore = null

            createdAt = LocalDateTime.now()
            createdBy = modifyUserId
        }
    }
}