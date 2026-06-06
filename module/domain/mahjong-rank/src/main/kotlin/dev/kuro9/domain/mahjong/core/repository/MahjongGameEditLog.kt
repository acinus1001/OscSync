package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.domain.mahjong.core.enums.MahjongLogType
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MahjongGameEditLogs : LongIdTable("mahjong_game_edit_log") {
    val game = reference(
        name = "game_id",
        foreign = MahjongGames,
        onUpdate = ReferenceOption.RESTRICT,
        onDelete = ReferenceOption.RESTRICT,
    )

    val type = enumeration<MahjongLogType>("type")

    val originalTouUserId = long("tou_user_id").nullable()
    val originalTouUserScore = integer("tou_user_score").nullable()
    val originalNanUserId = long("nan_user_id").nullable()
    val originalNanUserScore = integer("nan_user_score").nullable()
    val originalShaUserId = long("sha_user_id").nullable()
    val originalShaUserScore = integer("sha_user_score").nullable()
    val originalPeiUserId = long("pei_user_id").nullable()
    val originalPeiUserScore = integer("pei_user_score").nullable()

    val newTouUserId = long("new_tou_user_id").nullable()
    val newTouUserScore = integer("new_tou_user_score").nullable()
    val newNanUserId = long("new_nan_user_id").nullable()
    val newNanUserScore = integer("new_nan_user_score").nullable()
    val newShaUserId = long("new_sha_user_id").nullable()
    val newShaUserScore = integer("new_sha_user_score").nullable()
    val newPeiUserId = long("new_pei_user_id").nullable()
    val newPeiUserScore = integer("new_pei_user_score").nullable()


    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val createdBy = long("created_by")
}

class MahjongGameEditLogEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MahjongGameEditLogEntity>(MahjongGameEditLogs)

    var game by MahjongGameEditLogs.game
    var type by MahjongGameEditLogs.type

    var originalTouUserId by MahjongGameEditLogs.originalTouUserId
    var originalNanUserId by MahjongGameEditLogs.originalNanUserId
    var originalShaUserId by MahjongGameEditLogs.originalShaUserId
    var originalPeiUserId by MahjongGameEditLogs.originalPeiUserId
    var originalTouUserScore by MahjongGameEditLogs.originalTouUserScore
    var originalNanUserScore by MahjongGameEditLogs.originalNanUserScore
    var originalShaUserScore by MahjongGameEditLogs.originalShaUserScore
    var originalPeiUserScore by MahjongGameEditLogs.originalPeiUserScore

    var newTouUserId by MahjongGameEditLogs.newTouUserId
    var newNanUserId by MahjongGameEditLogs.newNanUserId
    var newShaUserId by MahjongGameEditLogs.newShaUserId
    var newPeiUserId by MahjongGameEditLogs.newPeiUserId
    var newTouUserScore by MahjongGameEditLogs.newTouUserScore
    var newNanUserScore by MahjongGameEditLogs.newNanUserScore
    var newShaUserScore by MahjongGameEditLogs.newShaUserScore
    var newPeiUserScore by MahjongGameEditLogs.newPeiUserScore

    var createdAt by MahjongGameEditLogs.createdAt
    var createdBy by MahjongGameEditLogs.createdBy
}