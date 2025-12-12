package dev.kuro9.domain.inquiry.service

import dev.kuro9.domain.inquiry.dto.InquiryEvent
import dev.kuro9.domain.inquiry.repository.table.Inquiries
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InquiryService(
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun save(
        userId: Long,
        guildId: Long?,
        channelId: Long,
        title: String,
        content: String,
        attachmentUrl: String?,
    ) {
        Inquiries.insert {
            it[this.userId] = userId
            it[this.guildId] = guildId
            it[this.channelId] = channelId
            it[this.title] = title
            it[this.content] = content
            it[this.attachmentUrl] = attachmentUrl
        }

        val event = InquiryEvent(
            userId = userId,
            guildId = guildId,
            channelId = channelId,
            title = title,
            content = content,
            attachmentUrl = attachmentUrl,
        )
        eventPublisher.publishEvent(event)
    }
}