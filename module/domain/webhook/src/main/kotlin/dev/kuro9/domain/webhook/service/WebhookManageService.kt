package dev.kuro9.domain.webhook.service

import dev.kuro9.common.exception.DuplicatedInsertException
import dev.kuro9.domain.webhook.enums.WebhookDomainType
import dev.kuro9.domain.webhook.enums.WebhookNotifyPhase
import dev.kuro9.domain.webhook.repository.table.WebhookNotifySendLogs
import dev.kuro9.domain.webhook.repository.table.WebhookSubscribeChannelEntity
import dev.kuro9.domain.webhook.repository.table.WebhookSubscribeChannels
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WebhookManageService {

    /**
     * 이미 등록된 채널인지 체크합니다.
     */
    fun getRegisteredChannel(domainType: WebhookDomainType, channelId: Long): WebhookSubscribeChannelEntity? {
        return WebhookSubscribeChannelEntity.findById(CompositeID {
            it[WebhookSubscribeChannels.EntityId.domainType] = domainType
            it[WebhookSubscribeChannels.EntityId.channelId] = channelId
        })
    }

    /**
     * 신곡 알림을 받을 채널을 등록합니다.
     * @throws DuplicatedInsertException 이미 등록된 채널일 때
     */
    @Transactional
    @Throws(DuplicatedInsertException::class)
    fun registerChannel(
        domainType: WebhookDomainType,
        channelId: Long,
        guildId: Long?,
        registerUserId: Long,
        webhookUrl: String,
        webhookId: Long,
    ) {
        val isInserted = WebhookSubscribeChannels.insertIgnore {
            it[WebhookSubscribeChannels.domainType] = domainType
            it[WebhookSubscribeChannels.channelId] = channelId
            it[WebhookSubscribeChannels.guildId] = guildId
            it[WebhookSubscribeChannels.webhookUrl] = webhookUrl
            it[WebhookSubscribeChannels.webhookId] = webhookId
            it[WebhookSubscribeChannels.registeredUserId] = registerUserId
            it[WebhookSubscribeChannels.createdAt] = LocalDateTime.now()
            it[WebhookSubscribeChannels.revokedAt] = null
        }.insertedCount == 1

        if (!isInserted) throw DuplicatedInsertException()
    }

    /**
     * 등록된 신곡 알림 채널을 삭제합니다.
     * @return true 시 삭제 성공, false 시 삭제할 채널 없음
     */
    @Transactional
    fun unregisterChannel(domainType: WebhookDomainType, channelId: Long): Boolean {
        return WebhookSubscribeChannels
            .deleteWhere {
                (WebhookSubscribeChannels.domainType eq domainType)
                    .and(WebhookSubscribeChannels.channelId eq channelId)
            } == 1
    }

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     * @param pageSize 페이지 사이즈
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllSubscribedChannels(
        domainType: WebhookDomainType,
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<WebhookSubscribeChannelEntity> {
        return when (lastChannelId) {
            null -> WebhookSubscribeChannelEntity.find { (WebhookSubscribeChannels.domainType eq domainType) }
            else -> WebhookSubscribeChannelEntity
                .find {
                    (WebhookSubscribeChannels.domainType eq domainType)
                        .and(WebhookSubscribeChannels.channelId greater lastChannelId)
                }
        }
            .orderBy(WebhookSubscribeChannels.channelId to SortOrder.ASC)
            .limit(pageSize)
            .toList()
    }

    fun getLatestSendDataSeq(domainType: WebhookDomainType, channelId: Long): Long? {
        return WebhookNotifySendLogs.select(WebhookNotifySendLogs.sendDataSeq)
            .where { WebhookNotifySendLogs.domainType eq domainType }
            .andWhere { WebhookNotifySendLogs.channelId eq channelId }
            .orderBy(WebhookNotifySendLogs.sendDataSeq to SortOrder.DESC_NULLS_LAST)
            .limit(1)
            .firstOrNull()
            ?.get(WebhookNotifySendLogs.sendDataSeq)
    }

    /**
     * 커서 방식의 페이징 처리된 채널 반환
     *
     * @param pageSize 페이지 사이즈
     * @param lastChannelId 이전 요청의 마지막 entity 의 channelId
     */
    fun getAllFilteredSubscribedChannels(
        domainType: WebhookDomainType,
        pageSize: Int = 1000,
        lastChannelId: Long? = null,
    ): List<WebhookSubscribeChannelEntity> {
        val op: Op<Boolean> = WebhookSubscribeChannels.domainType eq domainType
        return when (lastChannelId) {
            null -> WebhookSubscribeChannelEntity.find { op }
            else -> WebhookSubscribeChannelEntity
                .find { WebhookSubscribeChannels.channelId greater lastChannelId and op }
        }
            .orderBy(WebhookSubscribeChannels.channelId to SortOrder.ASC)
            .limit(pageSize)
            .toList()
    }

    /**
     * 람다 (sendDataDescription, sendDataSeq) 리턴
     */
    @Transactional(noRollbackFor = [Throwable::class])
    suspend fun executeWithLog(
        data: WebhookSubscribeChannelEntity,
        action: suspend (latestDataSeq: Long?, entity: WebhookSubscribeChannelEntity) -> Pair<String?, Long?>,
    ) {
        val latestDataSeq = getLatestSendDataSeq(data.domainType, data.channelId)

        val seq = WebhookNotifySendLogs.insertAndGetId {
            it[WebhookNotifySendLogs.domainType] = data.domainType
            it[WebhookNotifySendLogs.channelId] = data.channelId
            it[WebhookNotifySendLogs.guildId] = data.guildId
            it[WebhookNotifySendLogs.phase] = WebhookNotifyPhase.INIT
            it[WebhookNotifySendLogs.exception] = null
            it[WebhookNotifySendLogs.sendDate] = LocalDateTime.now()
        }.value

        try {
            val (sendDataDescription, sendDataSeq) = action(latestDataSeq, data)

            // success update
            WebhookNotifySendLogs.update(
                where = { WebhookNotifySendLogs.seq eq seq },
            ) {
                it[WebhookNotifySendLogs.phase] = WebhookNotifyPhase.SUCCESS
                it[WebhookNotifySendLogs.sendDataInfo] = sendDataDescription
                it[WebhookNotifySendLogs.sendDataSeq] = sendDataSeq
            }
        } catch (t: Throwable) {

            // failure update
            WebhookNotifySendLogs.update(
                where = { WebhookNotifySendLogs.seq eq seq },
            ) {
                it[WebhookNotifySendLogs.phase] = WebhookNotifyPhase.FAILURE
                it[WebhookNotifySendLogs.exception] = t.stackTraceToString().take(2500)
            }
        }
    }

}