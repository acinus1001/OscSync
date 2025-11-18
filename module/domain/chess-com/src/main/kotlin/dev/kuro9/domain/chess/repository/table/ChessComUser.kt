package dev.kuro9.domain.chess.repository.table

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object ChessComUsers : IdTable<Long>("chess_com_user") {
    val userId = long("user_id").entityId()
    val guildId = long("guild_id").nullable()
    val username = varchar("username", 255)
    val userProfileUrl = varchar("user_profile_url", 500)
    val profilePic = varchar("profile_pic_url", 500).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }

    override val id: Column<EntityID<Long>> = userId

    init {
        index(isUnique = false, guildId)
    }
}

class ChessComUserEntity(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, ChessComUserEntity>(ChessComUsers)

    var userId by ChessComUsers.userId
    var guildId by ChessComUsers.guildId
    var username by ChessComUsers.username
    var userProfileUrl by ChessComUsers.userProfileUrl
    var profilePic by ChessComUsers.profilePic
    var createdAt by ChessComUsers.createdAt
    var updatedAt by ChessComUsers.updatedAt
}