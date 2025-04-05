package dev.kuro9.domain.karaoke.repository.table

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object KaraokeSubscribeChannels : LongIdTable("karaoke_subscribe_channel", "channe_id") {
    val channelId by ::id
    val guildId = long("guild_id").nullable()
    val webhookUrl = varchar("webhook_url", 500)
    val registeredUserId = long("registered_user_id")
    val createdAt = datetime("created_at")
}

class KaraokeSubscribeChannelEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<KaraokeSubscribeChannelEntity>(KaraokeSubscribeChannels)

    val channelId by KaraokeSubscribeChannels.channelId
    val guildId by KaraokeSubscribeChannels.guildId
    val webhookUrl by KaraokeSubscribeChannels.webhookUrl
    val registeredUserId by KaraokeSubscribeChannels.registeredUserId
    val createdAt by KaraokeSubscribeChannels.createdAt
}