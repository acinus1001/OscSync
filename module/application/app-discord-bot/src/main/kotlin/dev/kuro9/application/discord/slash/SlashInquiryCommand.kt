package dev.kuro9.application.discord.slash

import dev.kuro9.domain.inquiry.service.InquiryService
import dev.kuro9.internal.discord.handler.model.ModalInteractionHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.modals.Modal
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.OffsetDateTime

@Component
class SlashInquiryCommand(
    private val inquiryService: InquiryService,
) : SlashCommandComponent, ModalInteractionHandler {
    override val commandData: SlashCommandData = slash("inquiry", "문의사항을 전송합니다.")

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val title = TextInput.create("title", TextInputStyle.SHORT)
            .setPlaceholder("제목")
            .setRequiredRange(0, 100)
            .build()

        val body = TextInput.create("body", TextInputStyle.PARAGRAPH)
            .setPlaceholder("내용")
            .setRequiredRange(1, 1000)
            .build()

        val file = AttachmentUpload.create("file")
            .setRequired(false)
            .build()

        val modal = Modal.create("inquiry", "문의사항")
            .addComponents(
                Label.of("제목", title),
                Label.of("내용", body),
                Label.of("파일", file)
            )
            .build()

        event.replyModal(modal).await()
    }

    override suspend fun isHandleable(event: ModalInteractionEvent): Boolean {
        return event.modalId == "inquiry"
    }

    override suspend fun handleModalInteraction(event: ModalInteractionEvent) {
        val title = event.getValue("title")?.asString ?: ""
        val body = event.getValue("body")?.asString ?: ""
        val file = event.getValue("file")?.asAttachmentList?.firstOrNull()
        val user = event.user.idLong

        info { "문의사항 제목: $title, 내용: $body, 파일 url: ${file?.url ?: "없음"} ,사용자 ID: $user" }
        withContext(Dispatchers.IO) {
            inquiryService.save(
                userId = event.user.idLong,
                guildId = event.guild?.idLong,
                channelId = event.channelIdLong,
                title = title,
                content = body,
                attachmentUrl = file?.url,
            )
        }

        Embed {
            this.title = "200 OK"
            this.description = "문의사항이 접수되었습니다. 감사합니다."
            this.timestamp = OffsetDateTime.now()
            this.color = Color.GREEN.rgb
        }.let { event.replyEmbeds(it).await() }
    }
}