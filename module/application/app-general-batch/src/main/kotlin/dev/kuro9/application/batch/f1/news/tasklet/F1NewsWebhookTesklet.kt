package dev.kuro9.application.batch.f1.news.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter
import dev.kuro9.application.batch.discord.dto.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.application.batch.f1.news.tasklet.dto.F1NewsTargetWebhookDto
import dev.kuro9.domain.f1.dto.F1NewsDto
import dev.kuro9.domain.f1.service.F1NewsService
import dev.kuro9.domain.webhook.enums.WebhookDomainType
import dev.kuro9.domain.webhook.repository.table.WebhookSubscribeChannelEntity
import dev.kuro9.domain.webhook.service.WebhookManageService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@[StepScope Component]
class F1NewsWebhookTesklet(
    private val webhookService: WebhookManageService,
    private val f1NewsService: F1NewsService,
    private val discordWebhookService: DiscordWebhookService,
) : ItemStreamIterableReaderWriter<F1NewsTargetWebhookDto> {
    private var lastChannelId: Long? = null

    override fun readIterable(p0: ExecutionContext): Iterable<F1NewsTargetWebhookDto> {
        return webhookService.getAllFilteredSubscribedChannels(
            domainType = WebhookDomainType.F1_NEWS,
            pageSize = 100,
            lastChannelId = lastChannelId
        ).mapNotNull { channel: WebhookSubscribeChannelEntity ->
            val lastSendDataSeq: Long? = webhookService.getLatestSendDataSeq(
                domainType = WebhookDomainType.F1_NEWS,
                channelId = channel.channelId
            )

            val newsList = f1NewsService.getAllNewsWithSummary(lastSendDataSeq)
                .takeIf { it.isNotEmpty() } ?: return@mapNotNull null

            F1NewsTargetWebhookDto(
                webhookInfo = channel,
                sendNewsInfoList = newsList,
            )
        }
    }

    override fun write(p0: Chunk<out F1NewsTargetWebhookDto>) {
        runBlocking {

            for ((channel, newsList) in p0) {

                for (news in newsList) {
                    webhookService.executeWithLog(channel) { _, _ ->
                        discordWebhookService.sendWebhookWithRetry(
                            channel.webhookUrl,
                            news.toWebhookPayload()
                        )
                        null to news.id
                    }

                }

            }
        }

    }

    private fun List<F1NewsDto>.toWebhookPayload(): List<DiscordWebhookPayload> {

        val embeds = this.map { news ->
            Embed {
                title = news.title
                description = news.href
                image = news.imageUrl

                Field {
                    name = "Content"
                    value = news.contentSummary
                    inline = false
                }
            }
        }

        return embeds.map { embed ->
            DiscordWebhookPayload(
                username = "KGB: F1 News Notify",
                embeds = listOf(embed),
            )
        }
    }

    private fun F1NewsDto.toWebhookPayload(): DiscordWebhookPayload {

        val embed = Embed {
            title = this@toWebhookPayload.title
            description = this@toWebhookPayload.href
            image = this@toWebhookPayload.imageUrl

            Field {
                name = "Content"
                value = this@toWebhookPayload.contentSummary
                inline = false
            }
        }

        return DiscordWebhookPayload(
            username = "KGB: F1 News Notify",
            embeds = listOf(embed),
        )
    }
}