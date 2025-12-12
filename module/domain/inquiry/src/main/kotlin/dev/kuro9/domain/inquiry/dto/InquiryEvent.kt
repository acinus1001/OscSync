package dev.kuro9.domain.inquiry.dto

data class InquiryEvent(
    val userId: Long,
    val guildId: Long?,
    val channelId: Long,
    val title: String,
    val content: String,
    val attachmentUrl: String? = null,
)
