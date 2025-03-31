package dev.kuro9.domain.karaoke.repository

import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannels
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
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
}