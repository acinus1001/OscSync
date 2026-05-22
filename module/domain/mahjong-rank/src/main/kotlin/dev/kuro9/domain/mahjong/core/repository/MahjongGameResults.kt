package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.domain.database.between
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object MahjongGameResults : LongIdTable("mahjong_game_result") {
    val userId = long("user_id")
    val rank = integer("rank").check { it.between(1..4) }

    val game = reference(
        "game_id",
        MahjongGames,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
}

class MahjongGameResultEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MahjongGameResultEntity>(MahjongGameResults)

    var userId by MahjongGameResults.userId
    var rank by MahjongGameResults.rank

    val game by MahjongGameEntity referencedOn MahjongGameResults.game
}