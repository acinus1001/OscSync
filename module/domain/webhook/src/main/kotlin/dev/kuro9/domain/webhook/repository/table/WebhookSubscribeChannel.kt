package dev.kuro9.domain.webhook.repository.table

import dev.kuro9.domain.webhook.enums.WebhookDomainType
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object WebhookSubscribeChannels : CompositeIdTable("webhook_subscribe_channel") {
    val domainType = enumerationByName<WebhookDomainType>("domain_type", 20)
    val channelId = long("channel_id")
    val guildId = long("guild_id").nullable()
    val webhookUrl = varchar("webhook_url", 500)
    val webhookId = long("webhook_id")
    val registeredUserId = long("registered_user_id")
    val createdAt = datetime("created_at")
    val revokedAt = datetime("revoked_at").nullable()

    object EntityId {
        val domainType = WebhookSubscribeChannels.domainType.entityId()
        val channelId = WebhookSubscribeChannels.channelId.entityId()
    }

    override val primaryKey = PrimaryKey(
        EntityId.domainType,
        EntityId.channelId
    )
}

class WebhookSubscribeChannelEntity(pk: EntityID<CompositeID>) : CompositeEntity(pk) {
    companion object : CompositeEntityClass<WebhookSubscribeChannelEntity>(WebhookSubscribeChannels)

    val domainType by WebhookSubscribeChannels.domainType
    val channelId by WebhookSubscribeChannels.channelId
    val guildId by WebhookSubscribeChannels.guildId
    val webhookUrl by WebhookSubscribeChannels.webhookUrl
    val webhookId by WebhookSubscribeChannels.webhookId
    val registeredUserId by WebhookSubscribeChannels.registeredUserId
    val createdAt by WebhookSubscribeChannels.createdAt
    val revokedAt by WebhookSubscribeChannels.revokedAt
}