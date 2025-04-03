package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemProcessor
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.discord.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.config.DiscordProperties
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeSongFetchTasklet
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeWebhookTasklet
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.repository.table.KaraokeNotifySendLog
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class KaraokeCrawlBatchConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager,
) {

    @Bean
    fun karaokeCrawlJob(
        fetchTasklet: KaraokeSongFetchTasklet,
    ): Job = batch {
        job("karaokeCrawlJob") {
            step(karaokeCrawlNewSongStep(fetchTasklet)) {
                on(ExitStatus.COMPLETED.exitCode) {
                    stepBean("karaokeNotifyNewSongStep")
                }
                on(ExitStatus.FAILED.exitCode) {
                    stepBean("karaokeCrawlFailureStep")
                }
            }
        }
    }

    @Bean
    fun karaokeCrawlNewSongStep(fetchTasklet: KaraokeSongFetchTasklet): Step = batch {
        step("karaokeCrawlNewSongStep") {
            chunk<KaraokeSongDto, KaraokeSongDto>(1000, txManager) {
                reader(fetchTasklet.asItemStreamReader())
                writer(fetchTasklet.asItemStreamWriter())

                faultTolerant {
                    retry<Throwable>()
                    retryLimit(3)
                    backOffPolicy(ExponentialBackOffPolicy())
                }
            }
        }
    }

    @Bean
    fun karaokeNotifyNewSongStep(webhookTasklet: KaraokeWebhookTasklet): Step = batch {
        step("karaokeNotifyNewSongStep") {
            chunk<KaraokeSubscribeChannelEntity, KaraokeNotifySendLog>(10, txManager) {
                reader(webhookTasklet.asItemStreamReader())
                processor(webhookTasklet.asItemProcessor())
                writer(webhookTasklet.asItemStreamWriter())
            }
        }
    }

    @Bean
    fun karaokeCrawlFailureStep(
        webhookService: DiscordWebhookService,
        discordProperties: DiscordProperties,
    ): Step = batch {
        step("karaokeCrawlFailureStep") {
            tasklet(transactionManager = txManager, tasklet = { sc: StepContribution, cc: ChunkContext ->
                runBlocking {
                    webhookService.sendWebhook(
                        discordProperties.errorUrl, payload = DiscordWebhookPayload(
                            username = "Batch Error Notify",
                            embeds = listOf(
                                Embed {
                                    title = "Batch Error Alert"
                                    description = this::class.simpleName
                                    color = 0xFF0000
                                }
                            )
                        )
                    )
                }

                RepeatStatus.FINISHED
            })
        }
    }
}