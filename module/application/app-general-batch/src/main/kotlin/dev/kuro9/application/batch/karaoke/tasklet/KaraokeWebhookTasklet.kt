package dev.kuro9.application.batch.karaoke.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter
import dev.kuro9.application.batch.discord.dto.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.domain.karaoke.service.KaraokeChannelService
import dev.kuro9.domain.karaoke.service.KaraokeNewSongService
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDate
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@StepScope
@Component
class KaraokeWebhookTasklet(
    private val webhookService: DiscordWebhookService,
    private val channelService: KaraokeChannelService,
    private val newSongService: KaraokeNewSongService,
    @Value("#{jobParameters['executeDate']}") private val _executeDate: LocalDate,
) : ItemStreamIterableReaderWriter<KaraokeSubscribeChannelEntity> {
    private val executeDate = _executeDate.toKotlinLocalDate()
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

    override fun write(chunk: Chunk<out KaraokeSubscribeChannelEntity>) {
        if (webhookPayload == null) {
            info { "WebhookPayload is null" }
            return
        }
        for (entity in chunk) {
            runBlocking {
                channelService.executeWithLog(entity) {
                    webhookService.sendWebhook(entity.webhookUrl, webhookPayload)
                }
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
            username = "KGB: Karaoke Notify",
            embeds = embedList
        )
    }

}