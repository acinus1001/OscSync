package dev.kuro9.application.batch.karaoke.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderProcessorWriter
import dev.kuro9.application.batch.discord.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLog
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.domain.karaoke.service.KaraokeChannelService
import dev.kuro9.domain.karaoke.service.KaraokeNewSongService
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@StepScope
@Component
class KaraokeWebhookTasklet(
    private val webhookService: DiscordWebhookService,
    private val channelService: KaraokeChannelService,
    private val newSongService: KaraokeNewSongService,
    @Value("#{jobParameters['executeDate']}") private val _executeDate: String,
) : ItemStreamIterableReaderProcessorWriter<KaraokeSubscribeChannelEntity, KaraokeNotifySendLog> {
    private val executeDate = kotlinx.datetime.LocalDate.parse(_executeDate)
    private var lastChannelId: Long? = null
    private val webhookPayload = makeWebhookPayload()

    override fun readIterable(context: ExecutionContext): Iterable<KaraokeSubscribeChannelEntity> {
        if (webhookPayload == null) return emptyList() // 데이터 없다면 종료

        return channelService.getAllFilteredSubscribedChannels(
            pageSize = 1000,
            lastChannelId = lastChannelId
        ).also {
            lastChannelId = it.lastOrNull()?.channelId?.value ?: return@also
        }
    }

    override fun process(channelEntity: KaraokeSubscribeChannelEntity): KaraokeNotifySendLog? {
        val requestTime = LocalDateTime.now()
        val exception = runCatching {
            runBlocking {
                webhookService.sendWebhook(channelEntity.webhookUrl, webhookPayload ?: return@runBlocking null)
            }
        }.exceptionOrNull()

        return KaraokeNotifySendLog(
            channelId = channelEntity.channelId.value,
            guildId = channelEntity.guildId,
            exception = exception,
            sendDate = requestTime,
        )
    }

    override fun write(chunk: Chunk<out KaraokeNotifySendLog>) {
        channelService.batchInsertLogs(chunk.items)
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
                title = "TJ ${executeDate.monthNumber}/${executeDate.dayOfMonth} 신곡 알림"
                description =
                    "$executeDate 09:00 데이터 : " + if (tjReleaseSongs.size > 25) "${i + 1}/${((tjReleaseSongs.size - 1) / 25) + 1}" else ""
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