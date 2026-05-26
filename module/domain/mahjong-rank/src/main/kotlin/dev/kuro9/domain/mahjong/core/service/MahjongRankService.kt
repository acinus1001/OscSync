package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.mahjong.core.dto.MahjongGameDetailInput
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
import dev.kuro9.domain.mahjong.core.event.MahjongRankEvent
import dev.kuro9.domain.mahjong.core.repository.*
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.network.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.selectAll
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
                this.rank = userScoreList.sortedWith(
                    compareByDescending<MahjongGameDetailInput> { it.score }
                        .thenBy { it.seki } // 동점이면 기가에 가까운 순
                ).indexOfFirst { it.userId == userId } + 1
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

    /**
     * 현재 dao 방식의 referresOn 컬럼에는 구 데이터가 들어가므로 참조하지 말 것.
     */
    fun modify(
        id: Long,
        modifyUserId: Long,
        vararg modifyData: MahjongGameDetailInput,
    ): Pair<MahjongGameEntity, List<MahjongGameResultEntity>> {
        require(modifyData.size == 4) { "userScoreList must have 4 elements" }
        require(modifyData.distinctBy { it.userId }.size == modifyData.size) { "유저는 중복될 수 없습니다." }
        require(modifyData.map { it.seki }
            .toSet() == MahjongSeki.entries.toSet() || modifyData.all { it.seki == null }) {
            "userScoreList must have unique seki or null"
        }
        require(modifyData.sumOf { it.score } == 10_0000) { "점수 합은 10만점 이어야 합니다." }

        val game = getGameById(id) ?: throw NoSuchElementException("id $id game not found")
        game.updatedBy = modifyUserId
        game.updatedAt = LocalDateTime.now()

        val sortedData = modifyData.sortedWith(
            compareByDescending<MahjongGameDetailInput> { it.score }
                .thenBy { it.seki } // 동점이면 기가에 가까운 순
        )

        game.results.forEach { it.delete() } // pk violate 피하기 위해 삭제 후 insert

        for ((userId: Long, score: Int, seki: MahjongSeki?) in sortedData) {
            MahjongGameResultEntity.new {
                this.game = game
                this.userId = userId
                this.score = score
                this.rank = sortedData.indexOfFirst { it.userId == userId } + 1
                this.seki = seki
            }
        }


        eventPublisher.publishEvent(
            MahjongRankEvent.ModifyGameResult(
                targetGuildId = game.guildId,
                createdAt = game.createdAt,
                userScoreList = game.results.map { it.toModel() }.sorted(),
            )
        )
        game.refresh(true)

        return MahjongGames.selectAll().where(MahjongGames.id eq id).single().let {
            MahjongGameEntity.wrapRow(it)
        } to MahjongGameResults.selectAll().where(MahjongGameResults.game eq game.id.value).map {
            MahjongGameResultEntity.wrapRow(it)
        }
    }

    fun delete(id: Long) {
        MahjongGameEntity.findById(id)?.delete()
    }

    @Transactional(readOnly = true)
    fun getGameById(id: Long): MahjongGameEntity? =
        MahjongGameEntity.findById(id)?.load(MahjongGameEntity::results, MahjongGameEntity::scoreSetting)


}