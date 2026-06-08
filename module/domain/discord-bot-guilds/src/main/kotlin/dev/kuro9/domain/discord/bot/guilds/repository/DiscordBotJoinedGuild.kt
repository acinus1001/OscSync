package dev.kuro9.domain.discord.bot.guilds.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object DiscordBotJoinedGuilds : LongIdTable("discord_bot_joined_guild") {
    val botId = long("bot_id")
    val guildId = long("guild_id")
    val guildName = varchar("guild_name", 255)
    val guildIconUrl = varchar("guild_icon", 512).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    init {
        uniqueIndex(botId, guildId)
    }
}

class DiscordBotJoinedGuildEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DiscordBotJoinedGuildEntity>(DiscordBotJoinedGuilds)

    var botId by DiscordBotJoinedGuilds.botId
    var guildId by DiscordBotJoinedGuilds.guildId
    var guildName by DiscordBotJoinedGuilds.guildName
    var guildIconUrl by DiscordBotJoinedGuilds.guildIconUrl
    var createdAt by DiscordBotJoinedGuilds.createdAt
}