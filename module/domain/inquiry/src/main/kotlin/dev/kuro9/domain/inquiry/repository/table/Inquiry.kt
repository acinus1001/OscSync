package dev.kuro9.domain.inquiry.repository.table

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object Inquiries : LongIdTable("inquiry") {
    val userId = long("user_id")
    val guildId = long("guild_id").nullable()
    val channelId = long("channel_id")
    val title = varchar("title", 256)
    val content = varchar("content", 4000)
    val attachmentUrl = varchar("attachment_url", 500).nullable()
}

class InquiryEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<InquiryEntity>(Inquiries)

    var userId by Inquiries.userId
    var guildId by Inquiries.guildId
    var channelId by Inquiries.channelId
    var title by Inquiries.title
    var content by Inquiries.content
    var attachmentUrl by Inquiries.attachmentUrl
}