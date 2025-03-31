package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.repository.KaraokeChannelRepo
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class KaraokeChannelService(private val channelRepo: KaraokeChannelRepo) {

    /**
     * 신곡 알림을 받을 채널을 등록합니다.
     * @return true 시 등록 성공, false 시 이미 등록된 채널
     */
    @Transactional
    fun registerChannel(
        channelId: Long,
        guildId: Long,
        registerUserId: Long,
        webhookUrl: String,
    ): Boolean = channelRepo.registerChannel(channelId, guildId, registerUserId, webhookUrl)

    /**
     * 등록된 신곡 알림 채널을 삭제합니다.
     * @return true 시 삭제 성공, false 시 삭제할 채널 없음
     */
    @Transactional
    fun unregisterChannel(channelId: Long): Boolean = channelRepo.unregisterChannel(channelId)

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     * @param pageSize 페이지 사이즈
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllSubscribedChannels(
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<KaraokeSubscribeChannelEntity> = channelRepo.getAllSubscribedChannels(pageSize, lastChannelId)
    
}