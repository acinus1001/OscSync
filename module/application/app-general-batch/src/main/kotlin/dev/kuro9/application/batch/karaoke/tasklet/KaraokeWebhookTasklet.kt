package dev.kuro9.application.batch.karaoke.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderProcessorWriter
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLog
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import dev.kuro9.domain.karaoke.service.KaraokeChannelService
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@StepScope
@Component
class KaraokeWebhookTasklet(
    private val webhookService: DiscordWebhookService,
    private val channelService: KaraokeChannelService,
) : ItemStreamIterableReaderProcessorWriter<KaraokeSubscribeChannelEntity, KaraokeNotifySendLog> {
    private var lastChannelId: Long? = null

    override fun readIterable(context: ExecutionContext): Iterable<KaraokeSubscribeChannelEntity> {
        return channelService.getAllSubscribedChannels(
            pageSize = 1000,
            lastChannelId = lastChannelId
        ).also {
            lastChannelId = it.lastOrNull()?.channelId?.value ?: return@also
        }
    }

    override fun process(channelEntity: KaraokeSubscribeChannelEntity): KaraokeNotifySendLog {
        TODO("send webhook and make log")
    }

    override fun write(chunk: Chunk<out KaraokeNotifySendLog?>) {
        TODO("save log, chunksize 1")
    }

}