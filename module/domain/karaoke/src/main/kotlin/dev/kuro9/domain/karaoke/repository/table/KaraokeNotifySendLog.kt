package dev.kuro9.domain.karaoke.repository.table

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime


object KaraokeNotifySendLogs : LongIdTable(name = "karaoke_notify_send_log", "seq") {
    val seq by ::id
    val channelId = long("channel_id")
    val guildId = long("guild_id").nullable()
    val phase = enumerationByName<KaraokeNotifyPhase>("phase", 10)
    val exception = text("exception").nullable()
    val sendDate = datetime("send_date")
}

class KaraokeNotifySendLogEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<KaraokeNotifySendLogEntity>(KaraokeNotifySendLogs)

    var seq by KaraokeNotifySendLogs.seq; private set
    var channelId by KaraokeNotifySendLogs.channelId; private set
    var guildId by KaraokeNotifySendLogs.guildId; private set
    var exception by KaraokeNotifySendLogs.exception; private set
    var sendDate by KaraokeNotifySendLogs.sendDate; private set
}

data class KaraokeNotifySendLog(
    val channelId: Long,
    val guildId: Long,
    val exception: Throwable? = null,
    val sendDate: LocalDateTime = LocalDateTime.now(),
)

enum class KaraokeNotifyPhase {
    INIT,
    SUCCESS,
    FAILURE;
}
