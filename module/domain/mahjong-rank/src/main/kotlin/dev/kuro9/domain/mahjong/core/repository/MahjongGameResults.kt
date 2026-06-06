package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.domain.database.between
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import java.math.BigDecimal

object MahjongGameResults : LongIdTable("mahjong_game_result") {
    val userId = long("user_id")
    val rank = integer("rank").check { it.between(1..4) }
    val score = integer("score")
    val seki = enumeration<MahjongSeki>("seki").nullable()

    val game = reference(
        "game_id",
        MahjongGames,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )

    init {
        uniqueIndex(game, userId)
        uniqueIndex(game, rank)
    }
}

class MahjongGameResultEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MahjongGameResultEntity>(MahjongGameResults)

    var userId by MahjongGameResults.userId
    var rank by MahjongGameResults.rank
    var score by MahjongGameResults.score
    var seki by MahjongGameResults.seki

    var game by MahjongGameEntity referencedOn MahjongGameResults.game

    /* calculated fields */
    val point: BigDecimal
        get() {
            return with(game.scoreSetting) {
                (((score - returnScore) + (getUma(rank) * 1000) + getOka(rank)) / 100L)
            }.let { BigDecimal.valueOf(it, 1) }
        }

}

data class MahjongGameResultModel(
    val userId: Long,
    val rank: Int,
    val score: Int,
    val seki: MahjongSeki?,
) : Comparable<MahjongGameResultModel> {
    override fun compareTo(other: MahjongGameResultModel): Int = rank.compareTo(other.rank)

    init {
        require(rank in 1..4) { "Rank must be between 1 and 4" }
    }
}

fun MahjongGameResultEntity.toModel(): MahjongGameResultModel =
    MahjongGameResultModel(userId = userId, rank = rank, score = score, seki = seki)