package dev.kuro9.domain.mahjong.core.service

import dev.kuro9.domain.mahjong.core.dto.MahjongGameDeleteInfo
import dev.kuro9.domain.mahjong.core.dto.MahjongGameDetailInput
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
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
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class MahjongRankService(
    private val scoreService: MahjongScoreSettingService,
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

        game.refresh(true)

        return MahjongGames.selectAll().where(MahjongGames.id eq id).single().let {
            MahjongGameEntity.wrapRow(it)
        } to MahjongGameResults.selectAll().where(MahjongGameResults.game eq game.id.value).map {
            MahjongGameResultEntity.wrapRow(it)
        }
    }

    fun delete(id: Long, modifyUserId: Long): MahjongGameDeleteInfo? {
        val game = MahjongGameEntity.findById(id) ?: return null
        val model = MahjongGameDeleteInfo(
            guildId = game.guildId,
            gameId = game.id.value,
            gameUserIdSet = game.results.map { it.userId }.toSet(),
            gameCreatedAt = game.createdAt,
        )
        game.deletedAt = LocalDateTime.now() // soft delete
        game.updatedBy = modifyUserId
        game.updatedAt = LocalDateTime.now()
        return model
    }

    @Transactional(readOnly = true)
    fun getGameById(id: Long, nullsOnDeleted: Boolean = true): MahjongGameEntity? =
        MahjongGameEntity.findById(id)
            ?.takeIf { !nullsOnDeleted || it.deletedAt == null }
            ?.load(MahjongGameEntity::results, MahjongGameEntity::scoreSetting)

    @Transactional(readOnly = true)
    fun getGameLogById(gameId: Long): List<MahjongGameEditLogEntity> = // 시간순 정렬됨
        MahjongGameEntity
            .find { MahjongGames.id eq gameId }
            .singleOrNull()
            ?.takeIf { it.deletedAt == null }
            ?.editLogs
            ?.toList()
            ?: emptyList()
}