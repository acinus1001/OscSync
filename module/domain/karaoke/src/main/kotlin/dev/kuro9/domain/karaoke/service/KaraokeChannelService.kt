package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.repository.KaraokeChannelRepo
import dev.kuro9.domain.karaoke.repository.KaraokeLogRepo
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLog
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class KaraokeChannelService(
    private val channelRepo: KaraokeChannelRepo,
    private val logRepo: KaraokeLogRepo
) {

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

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     * 오늘 한번 정상적으로 전송된 로그가 있다면 전송하지 않음 (exception is null)
     *
     * @param targetDate 전송하는 날짜
     * @param pageSize 페이지 사이즈
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllFilteredSubscribedChannels(
        targetDate: LocalDate = LocalDate.now(),
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<KaraokeSubscribeChannelEntity> =
        channelRepo.getAllFilteredSubscribedChannels(targetDate, pageSize, lastChannelId)

    @Transactional(noRollbackFor = [Throwable::class])
    suspend fun executeWithLog(
        data: KaraokeSubscribeChannelEntity,
        action: suspend (KaraokeSubscribeChannelEntity) -> Unit,
    ) {
        val seq = logRepo.initLog(data.channelId.value, data.guildId)

        try {
            action(data)
            logRepo.markAsSuccess(seq)
        } catch (t: Throwable) {
            logRepo.updateException(seq, t)
        }
    }

    @Transactional
    fun batchInsertLogs(logs: List<KaraokeNotifySendLog>) = logRepo.batchInsertLog(logs)
}