package dev.kuro9.domain.karaoke.repository

import dev.kuro9.domain.database.between
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLogs
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannels
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.date.util.toRangeOfDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class KaraokeChannelRepo {

    /**
     * 신곡 알림을 받을 채널을 등록합니다.
     * @return true 시 등록 성공, false 시 이미 등록된 채널
     */
    fun registerChannel(
        channelId: Long,
        guildId: Long,
        registerUserId: Long,
        webhookUrl: String,
    ): Boolean {
        return KaraokeSubscribeChannels.insertIgnore {
            it[KaraokeSubscribeChannels.channelId] = channelId
            it[KaraokeSubscribeChannels.guildId] = guildId
            it[KaraokeSubscribeChannels.webhookUrl] = webhookUrl
            it[KaraokeSubscribeChannels.registeredUserId] = registerUserId
            it[KaraokeSubscribeChannels.createdAt] = LocalDateTime.now()
        }.insertedCount == 1
    }

    /**
     * 등록된 신곡 알림 채널을 삭제합니다.
     * @return true 시 삭제 성공, false 시 삭제할 채널 없음
     */
    fun unregisterChannel(channelId: Long): Boolean {
        return KaraokeSubscribeChannels
            .deleteWhere { KaraokeSubscribeChannels.channelId eq channelId } == 1
    }

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllSubscribedChannels(
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<KaraokeSubscribeChannelEntity> {
        return when (lastChannelId) {
            null -> KaraokeSubscribeChannelEntity.all()
            else -> KaraokeSubscribeChannelEntity
                .find { KaraokeSubscribeChannels.channelId greater lastChannelId }
        }
            .orderBy(KaraokeSubscribeChannels.channelId to SortOrder.ASC)
            .limit(pageSize)
            .toList()
    }

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     * 오늘 한번 정상적으로 전송된 로그가 있다면 전송하지 않음 (exception is null)
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllFilteredSubscribedChannels(
        targetDate: LocalDate = LocalDate.now(),
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<KaraokeSubscribeChannelEntity> {
        val op: Op<Boolean> = KaraokeNotifySendLogs.select(intLiteral(1))
            .where(KaraokeNotifySendLogs.channelId eq KaraokeSubscribeChannels.channelId)
            .andWhere { KaraokeNotifySendLogs.sendDate between targetDate.toRangeOfDay() }
            .andWhere { KaraokeNotifySendLogs.exception.isNull() }
            .let(::notExists)
        return when (lastChannelId) {
            null -> KaraokeSubscribeChannelEntity.find { op }
            else -> KaraokeSubscribeChannelEntity
                .find { KaraokeSubscribeChannels.channelId greater lastChannelId and op }
        }
            .orderBy(KaraokeSubscribeChannels.channelId to SortOrder.ASC)
            .limit(pageSize)
            .toList()
    }
}