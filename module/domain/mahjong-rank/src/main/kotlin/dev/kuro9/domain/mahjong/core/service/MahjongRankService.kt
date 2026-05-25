package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.mahjong.core.dto.MahjongGameDetailInput
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
import dev.kuro9.domain.mahjong.core.event.MahjongRankEvent
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultEntity
import dev.kuro9.domain.mahjong.core.repository.MahjongGameResultModel
import dev.kuro9.domain.mahjong.core.repository.toModel
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.network.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.load
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class MahjongRankService(
    private val scoreService: MahjongScoreSettingService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val httpClient = httpClient()

    fun save(
        createdUserId: Long,
        createdGuildId: Long,
        imageUrl: String? = null,
        imageMediaType: MediaType? = MediaType.APPLICATION_OCTET_STREAM,
        vararg userScoreList: MahjongGameDetailInput,
    ): MahjongGameEntity {
        require(userScoreList.size == 4) { "userScoreList must have 4 elements" }
        require(userScoreList.distinctBy { it.userId }.size == userScoreList.size) { "유저는 중복될 수 없습니다." }
        require(userScoreList.map { it.seki }
            .toSet() == MahjongSeki.entries.toSet() || userScoreList.all { it.seki == null }) {
            "userScoreList must have unique seki or null"
        }
        require(userScoreList.sumOf { it.score } == 10_0000) { "점수 합은 10만점 이어야 합니다." }

        val imageBytes = imageUrl?.let { runBlocking { httpClient.get(it).readRawBytes() } }

        val latestScoreSetting = scoreService.getLatestScoreSetting(createdGuildId)

        val game = MahjongGameEntity.new {
            this.guildId = createdGuildId
            this.scoreSetting = latestScoreSetting
            this.image = imageBytes?.let(::ExposedBlob)
            this.imageMime = imageMediaType?.toString()
            this.createdBy = createdUserId
            this.updatedBy = createdUserId
        }
        for ((userId: Long, score: Int, seki: MahjongSeki?) in userScoreList) {
            MahjongGameResultEntity.new {
                this.game = game
                this.userId = userId
                this.score = score
                this.rank = userScoreList.sortedByDescending { it.score }.indexOfFirst { it.userId == userId } + 1
                this.seki = seki
            }
        }

        eventPublisher.publishEvent(
            MahjongRankEvent.NewGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = game.results.map { it.toModel() }.sorted()
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