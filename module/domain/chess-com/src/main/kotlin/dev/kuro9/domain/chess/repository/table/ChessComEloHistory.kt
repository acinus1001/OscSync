package dev.kuro9.domain.chess.repository.table

import dev.kuro9.domain.chess.enums.EloType
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ChessComEloHistories : Table("chess_com_elo_history") {
    val userId = long("user_id")
    val eloType = enumerationByName<EloType>("elo_type", 10)
    val elo = integer("elo")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    init {
        index(isUnique = false, userId, eloType, createdAt)
    }
}