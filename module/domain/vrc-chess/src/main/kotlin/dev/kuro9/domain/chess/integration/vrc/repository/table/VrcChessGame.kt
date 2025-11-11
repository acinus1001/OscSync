package dev.kuro9.domain.chess.integration.vrc.repository.table

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object VrcChessGames : LongIdTable("vrc_chess_game") {

    val whiteUserIpHash = integer("white_user_ip_hash")
    val blackUserIpHash = integer("black_user_ip_hash")
    val botELO = integer("bot_elo").nullable()

    val fen = varchar("fen", 100)

    val createdAt = datetime("created_at")
    val fenUpdatedAt = datetime("fen_updated_at")
    val endedAt = datetime("ended_at").nullable()

    init {
        index(isUnique = false, whiteUserIpHash, blackUserIpHash, endedAt)
    }
}

class VrcChessGameEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<VrcChessGameEntity>(VrcChessGames)

    var whiteUserIpHash by VrcChessGames.whiteUserIpHash
    var blackUserIpHash by VrcChessGames.blackUserIpHash
    var botELO by VrcChessGames.botELO

    var fen by VrcChessGames.fen

    var createdAt by VrcChessGames.createdAt
    var fenUpdatedAt by VrcChessGames.fenUpdatedAt
    var endedAt by VrcChessGames.endedAt
}