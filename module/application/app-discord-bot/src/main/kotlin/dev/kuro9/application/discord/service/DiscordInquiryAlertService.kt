package dev.kuro9.application.discord.service

import dev.kuro9.domain.inquiry.dto.InquiryEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DiscordInquiryAlertService(
    private val jda: JDA
) {

    @EventListener
    suspend fun onInquiryReceived(event: InquiryEvent) {
        val channel = jda.getTextChannelById(1448861471097229432L)!!

        val embed = Embed {
            title = "New Inquiry Received"
            description = "User <@!${event.userId}>"
            thumbnail = jda.getUserById(event.userId)!!.avatarUrl

            field {
                name = event.title
                value = event.content
            }

            image = event.attachmentUrl
        }
        channel.sendMessageEmbeds(embed).await()
    }
}