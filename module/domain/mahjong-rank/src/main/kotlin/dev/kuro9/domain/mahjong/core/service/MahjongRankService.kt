package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.mahjong.core.event.MahjongRankEvent
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultModel
import dev.kuro9.domain.mahjong.core.repository.toModel
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.dao.load
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class MahjongRankService(
    private val scoreService: MahjongScoreSettingService,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun save(
        createdUserId: Long,
        createdGuildId: Long,
        firstUserId: Long,
        firstScore: Int,
        secondUserId: Long,
        secondScore: Int,
        thirdUserId: Long,
        thirdScore: Int,
        fourthUserId: Long,
        fourthScore: Int,
    ) = save(
        createdUserId = createdUserId,
        createdGuildId = createdGuildId,
        userScoreList = listOf(
            MahjongGameResultModel(userId = firstUserId, rank = 1, score = firstScore),
            MahjongGameResultModel(userId = secondUserId, rank = 2, score = secondScore),
            MahjongGameResultModel(userId = thirdUserId, rank = 3, score = thirdScore),
            MahjongGameResultModel(userId = fourthUserId, rank = 4, score = fourthScore),
        )
    )

    fun save(
        createdUserId: Long,
        createdGuildId: Long,
        firstPlace: MahjongGameResultModel,
        secondPlace: MahjongGameResultModel,
        thirdPlace: MahjongGameResultModel,
        fourthPlace: MahjongGameResultModel,
    ): MahjongGameEntity = save(
        createdUserId = createdUserId,
        createdGuildId = createdGuildId,
        userScoreList = listOf(firstPlace, secondPlace, thirdPlace, fourthPlace)
    )

    fun save(
        createdUserId: Long,
        createdGuildId: Long,
        userScoreList: List<MahjongGameResultModel>,
    ): MahjongGameEntity {
        require(userScoreList.size == 4) { "userScoreList must have 4 elements" }
        require(userScoreList.distinctBy { it.userId }.size == userScoreList.size) { "userScoreList must have unique userId" }
        require(userScoreList.sortedByDescending { it.score }
            .map { it.rank } == (1..4).toList()) { "userScoreList must have sorted rank from 1 to 4" }

        val latestScoreSetting = scoreService.getLatestScoreSetting(createdGuildId)

        val game = MahjongGameEntity.new {
            this.guildId = createdGuildId
            this.scoreSetting = latestScoreSetting
            this.createdBy = createdUserId
            this.updatedBy = createdUserId
        }
        for ((userId, rank, score) in userScoreList) {
            MahjongGameResultEntity.new {
                this.game = game
                this.userId = userId
                this.score = score
                this.rank = rank
            }
        }

        eventPublisher.publishEvent(
            MahjongRankEvent.NewGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = userScoreList.sorted()
            )
        )

        return game.load(MahjongGameEntity::results, MahjongGameEntity::scoreSetting)
    }

    fun modify(
        id: Long,
        modifyUserId: Long,
        firstUserAndScore: MahjongGameResultModel? = null,
        secondUserAndScore: MahjongGameResultModel? = null,
        thirdUserAndScore: MahjongGameResultModel? = null,
        fourthUserAndScore: MahjongGameResultModel? = null,
    ): MahjongGameEntity {
        val game = getGameById(id) ?: throw NoSuchElementException("id $id game not found")
        game.updatedBy = modifyUserId
        game.updatedAt = LocalDateTime.now()

        fun updateResultOrSkip(userAndScore: MahjongGameResultModel?) {
            if (userAndScore == null) return
            val (userId, rank, score) = userAndScore
            game.results.firstOrNull { it.rank == rank }?.apply {
                this.userId = userId
                this.score = score
            }
        }

        updateResultOrSkip(firstUserAndScore)
        updateResultOrSkip(secondUserAndScore)
        updateResultOrSkip(thirdUserAndScore)
        updateResultOrSkip(fourthUserAndScore)

        eventPublisher.publishEvent(
            MahjongRankEvent.ModifyGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = game.results.map { it.toModel() }.sorted(),
                modifiedDataSet = setOfNotNull(
                    firstUserAndScore,
                    secondUserAndScore,
                    thirdUserAndScore,
                    fourthUserAndScore
                )
            )
        )

        return game
    }

    @Transactional(readOnly = true)
    fun getGameById(id: Long): MahjongGameEntity? =
        MahjongGameEntity.findById(id)?.load(MahjongGameEntity::results, MahjongGameEntity::scoreSetting)


}