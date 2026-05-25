package dev.kuro9.domain.mahjong.core.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MahjongGames : LongIdTable("mahjong_game") {
    val guildId = long("guild_id")

    val scoreSetting = reference("score_setting", MahjongScoreSettings)

    val image = blob("image").nullable()
    val imageMime = varchar("image_mime", 50).nullable()

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val createdBy = long("created_by")
    val updatedBy = long("updated_by")
}

class MahjongGameEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MahjongGameEntity>(MahjongGames)

    var guildId by MahjongGames.guildId

    var scoreSetting by MahjongScoreSettingEntity referencedOn MahjongGames.scoreSetting
    var image by MahjongGames.image
    var imageMime by MahjongGames.imageMime

    var createdAt by MahjongGames.createdAt
    var updatedAt by MahjongGames.updatedAt
    var createdBy by MahjongGames.createdBy
    var updatedBy by MahjongGames.updatedBy

    // N+1 발생 시 : `MahjongGameEntity.all().with(MahjongGameEntity::results)` 통해 방지
    val results by MahjongGameResultEntity.referrersOn(MahjongGameResults.game)
        .orderBy(MahjongGameResults.rank to SortOrder.ASC)
}