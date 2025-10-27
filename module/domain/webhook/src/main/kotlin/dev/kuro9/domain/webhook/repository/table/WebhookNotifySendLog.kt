package dev.kuro9.domain.webhook.repository.table

import dev.kuro9.domain.webhook.enums.WebhookDomainType
import dev.kuro9.domain.webhook.enums.WebhookNotifyPhase
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object WebhookNotifySendLogs : LongIdTable(name = "webhook_notify_send_log", "seq") {
    val seq by ::id
    val domainType = enumerationByName<WebhookDomainType>("domain_type", 20)
    val channelId = long("channel_id")
    val guildId = long("guild_id").nullable()
    val phase = enumerationByName<WebhookNotifyPhase>("phase", 10)
    val exception = text("exception").nullable()
    val sendDate = datetime("send_date")
    val sendDataInfo = varchar("send_data_info", 100).nullable()
    val sendDataSeq = long("send_data_seq").nullable()

    init {
        index(isUnique = false, domainType, channelId)
        index(isUnique = false, domainType, channelId, sendDataSeq)
    }
}

class WebhookNotifySendLogEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<WebhookNotifySendLogEntity>(WebhookNotifySendLogs)

    var domainType by WebhookNotifySendLogs.domainType; private set
    var seq by WebhookNotifySendLogs.seq; private set
    var channelId by WebhookNotifySendLogs.channelId; private set
    var guildId by WebhookNotifySendLogs.guildId; private set
    var exception by WebhookNotifySendLogs.exception; private set
    var sendDate by WebhookNotifySendLogs.sendDate; private set
    var sendDataInfo by WebhookNotifySendLogs.sendDataInfo; private set
    var sendDataSeq by WebhookNotifySendLogs.sendDataSeq; private set
}

data class WebhookNotifySendLog(
    val domainType: WebhookDomainType,
    val channelId: Long,
    val guildId: Long,
    val exception: Throwable? = null,
    val sendDate: LocalDateTime = LocalDateTime.now(),
    val sendDataInfo: String? = null,
    val sendDataSeq: Long? = null,
)
