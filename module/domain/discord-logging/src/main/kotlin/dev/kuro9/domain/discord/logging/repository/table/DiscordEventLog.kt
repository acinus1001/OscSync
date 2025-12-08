package dev.kuro9.domain.discord.logging.repository.table

import dev.kuro9.domain.discord.logging.enums.DiscordEventType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object DiscordEventLogs : LongIdTable("discord_event_log") {
    val userId = long("user_id")
    val guildId = long("guild_id").nullable()
    val channelId = long("channel_id")
    val type = enumerationByName<DiscordEventType>("type", 20)
    val command = varchar("command", 255)
    val args = text("args")
    val requestAt = datetime("request_at")
    val createdAt = datetime("created_at")

    init {
        index(isUnique = false, guildId, userId, command)
    }
}

class DiscordEventLogEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DiscordEventLogEntity>(DiscordEventLogs)

    var userId by DiscordEventLogs.userId
    var guildId by DiscordEventLogs.guildId
    var channelId by DiscordEventLogs.channelId
    var type by DiscordEventLogs.type
    var command by DiscordEventLogs.command
    var args by DiscordEventLogs.args
    var requestAt by DiscordEventLogs.requestAt
    var createdAt by DiscordEventLogs.createdAt
}