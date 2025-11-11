package dev.kuro9.domain.chess.integration.vrc.repository.table

import dev.kuro9.domain.chess.integration.vrc.enums.ChessTurn
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object VrcChessGameLogs : LongIdTable("vrc_chess_game_log") {
    val userIpHash = integer("user_ip_hash") // ip 저장하면 안된대서 hashCode() 호출해 사용
    val gameId = long("game_id")
    val fen = varchar("fen", 100)
    val move = varchar("move", 10)
    val pgn = varchar("pgn", 10)

    val turnType = enumerationByName<ChessTurn>("turn_type", 5)
    val isBotMove = bool("is_bot_move")

    val createdAt = datetime("created_at")
    val undoAt = datetime("undo_at").nullable() // 수 물리기 했을때 non null

    init {
        index(isUnique = false, gameId, createdAt, undoAt)
    }
}

class VrcChessGameLogEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<VrcChessGameLogEntity>(VrcChessGameLogs)

    var userIpHash by VrcChessGameLogs.userIpHash
    var gameId by VrcChessGameLogs.gameId
    var fen by VrcChessGameLogs.fen
    var move by VrcChessGameLogs.move
    var pgn by VrcChessGameLogs.pgn

    var turnType by VrcChessGameLogs.turnType
    var isBotMove by VrcChessGameLogs.isBotMove

    var createdAt by VrcChessGameLogs.createdAt
    var undoAt by VrcChessGameLogs.undoAt
}