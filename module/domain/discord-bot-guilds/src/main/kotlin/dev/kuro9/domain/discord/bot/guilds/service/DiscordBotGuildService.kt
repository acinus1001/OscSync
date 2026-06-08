package dev.kuro9.domain.discord.bot.guilds.service

import dev.kuro9.domain.discord.bot.guilds.repository.DiscordBotJoinedGuildEntity
import dev.kuro9.domain.discord.bot.guilds.repository.DiscordBotJoinedGuilds
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.upsert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@[Transactional Service]
class DiscordBotGuildService {

    fun joinGuildUpsert(
        botId: Long,
        guildId: Long,
        guildName: String,
        guildIconUrl: String?
    ) {
        DiscordBotJoinedGuilds.upsert(
            DiscordBotJoinedGuilds.botId,
            DiscordBotJoinedGuilds.guildId,

            onUpdateExclude = listOf(DiscordBotJoinedGuilds.createdAt)
        ) {
            it[this.botId] = botId
            it[this.guildId] = guildId
            it[this.guildName] = guildName
            it[this.guildIconUrl] = guildIconUrl
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    fun leaveGuild(botId: Long, guildId: Long) {
        DiscordBotJoinedGuilds.deleteWhere(limit = null) {
            (DiscordBotJoinedGuilds.botId eq botId).and(
                DiscordBotJoinedGuilds.guildId eq guildId
            )
        }
    }

    fun findGuildsByBotId(botId: Long): SizedIterable<DiscordBotJoinedGuildEntity> {
        return DiscordBotJoinedGuildEntity.find(DiscordBotJoinedGuilds.botId eq botId)
    }

    fun findGuildsByBotIdList(botId: Long) = findGuildsByBotId(botId).toList()

    fun findGuildsByBotIdAndGuildIdList(botId: Long, guildIdList: List<Long>): List<DiscordBotJoinedGuildEntity> {
        return DiscordBotJoinedGuildEntity.find {
            (DiscordBotJoinedGuilds.botId eq botId) and (DiscordBotJoinedGuilds.guildId inList guildIdList)
        }.toList()
    }
}