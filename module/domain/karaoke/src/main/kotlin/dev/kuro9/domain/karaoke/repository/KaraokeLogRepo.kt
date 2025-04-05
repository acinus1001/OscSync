package dev.kuro9.domain.karaoke.repository

import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifyPhase
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLog
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLogs
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class KaraokeLogRepo {

    /**
     * 로그 삽입
     * @return 삽입된 로그의 pk
     */
    fun initLog(
        channelId: Long,
        guildId: Long?,
    ): Long {
        return KaraokeNotifySendLogs.insertAndGetId {
            it[KaraokeNotifySendLogs.channelId] = channelId
            it[KaraokeNotifySendLogs.guildId] = guildId
            it[KaraokeNotifySendLogs.phase] = KaraokeNotifyPhase.INIT
            it[KaraokeNotifySendLogs.exception] = null
            it[KaraokeNotifySendLogs.sendDate] = LocalDateTime.now()
        }.value
    }

    fun markAsSuccess(seq: Long) {
        KaraokeNotifySendLogs.update(
            where = { KaraokeNotifySendLogs.seq eq seq },
        ) {
            it[KaraokeNotifySendLogs.phase] = KaraokeNotifyPhase.SUCCESS
        }
    }

    fun updateException(seq: Long, t: Throwable) {
        KaraokeNotifySendLogs.update(
            where = { KaraokeNotifySendLogs.seq eq seq },
        ) {
            it[KaraokeNotifySendLogs.phase] = KaraokeNotifyPhase.FAILURE
            it[KaraokeNotifySendLogs.exception] = t.stackTraceToString().take(2500)
        }
    }

    fun batchInsertLog(logs: List<KaraokeNotifySendLog>) {
        KaraokeNotifySendLogs.batchInsert(logs, ignore = true) { log ->
            this[KaraokeNotifySendLogs.channelId] = log.channelId
            this[KaraokeNotifySendLogs.guildId] = log.guildId
            this[KaraokeNotifySendLogs.phase] =
                if (log.exception != null) KaraokeNotifyPhase.FAILURE else KaraokeNotifyPhase.SUCCESS
            this[KaraokeNotifySendLogs.exception] = log.exception?.stackTraceToString()?.take(2500)
            this[KaraokeNotifySendLogs.sendDate] = log.sendDate
        }
    }
}