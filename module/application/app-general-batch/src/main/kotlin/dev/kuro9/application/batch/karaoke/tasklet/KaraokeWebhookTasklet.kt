package dev.kuro9.application.batch.karaoke.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter
import dev.kuro9.application.batch.discord.dto.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.service.KaraokeNewSongService
import dev.kuro9.domain.webhook.enums.WebhookDomainType
import dev.kuro9.domain.webhook.repository.table.WebhookSubscribeChannelEntity
import dev.kuro9.domain.webhook.service.WebhookManageService
import dev.kuro9.multiplatform.common.date.util.now
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@StepScope
@Component
class KaraokeWebhookTasklet(
    private val webhookService: DiscordWebhookService,
    private val newSongService: KaraokeNewSongService,
    private val webhookManageService: WebhookManageService,
    @param:Value("#{jobParameters['executeDate']}") private val _executeDate: java.time.LocalDate,
) : ItemStreamIterableReaderWriter<WebhookSubscribeChannelEntity> {
    private val executeDate = _executeDate.toKotlinLocalDate()
    private var lastChannelId: Long? = null
    private val webhookPayload = makeWebhookPayload()
    private val isAm = LocalTime.now().hour in 0..11

    override fun readIterable(context: ExecutionContext): Iterable<WebhookSubscribeChannelEntity> {
        if (webhookPayload == null) return emptyList() // 데이터 없다면 종료

        return webhookManageService.getAllFilteredSubscribedChannels(
            domainType = WebhookDomainType.KARAOKE,
            pageSize = 1000,
            lastChannelId = lastChannelId
        ).also {
            lastChannelId = it.lastOrNull()?.channelId ?: return@also
        }
    }

    override fun write(chunk: Chunk<out WebhookSubscribeChannelEntity>) {
        if (webhookPayload == null) {
            info { "WebhookPayload is null" }
            return
        }
        for (entity in chunk) {
            val lastLog = webhookManageService.getLastestSendLog(WebhookDomainType.KARAOKE, entity.channelId)
            val info = if (isAm) "AM" else "PM"

            if (lastLog?.sendDataInfo == info) continue

            webhookManageService.executeWithLog(entity) { _, _ ->
                runBlocking { webhookService.sendWebhook(entity.webhookUrl, webhookPayload) }

                info to null
            }
        }
    }

    private fun makeWebhookPayload(): DiscordWebhookPayload? {
        val tjReleaseSongs = newSongService.getNewReleaseSongs(
            brand = KaraokeBrand.TJ,
            targetDate = executeDate,
        )
            .takeIf { it.isNotEmpty() }
            ?: return null

        // field 25개, embed 10개 제한
        val embedList = tjReleaseSongs.chunked(25).mapIndexed { i, songChunk ->
            Embed {
                title = "TJ ${executeDate.month.number}/${executeDate.day} 신곡 알림"
                description =
                    "$executeDate ${if (isAm) "오전" else "오후"} 데이터 : " + if (tjReleaseSongs.size > 25) "${i + 1}/${((tjReleaseSongs.size - 1) / 25) + 1}" else ""
                songChunk.forEach { song ->
                    Field {
                        name = "[${song.songNo}] ${song.title}"
                        value = song.singer
                        inline = false
                    }
                }
            }
        }


        return DiscordWebhookPayload(
            username = "AGB: Karaoke Notify",
            embeds = embedList
        )
    }

}