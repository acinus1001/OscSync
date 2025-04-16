package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.discord.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.config.DiscordProperties
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeSongFetchTasklet
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeWebhookTasklet
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.*
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
                    end()
                }
                on(ExitStatus.FAILED.exitCode) {
                    stepBean("karaokeCrawlFailureStep")
                    fail()
                }
            }
        }
    }

    @Bean
    fun karaokeCrawlNewSongStep(fetchTasklet: KaraokeSongFetchTasklet): Step = batch {
        step("karaokeCrawlNewSongStep") {
            allowStartIfComplete(true)
            chunk<KaraokeSongDto, KaraokeSongDto>(1000, txManager) {
                reader(fetchTasklet.asItemStreamReader())
                writer(fetchTasklet.asItemStreamWriter())

                listener(object : ChunkListener {
                    override fun afterChunkError(context: ChunkContext) {
                        val exception = context.stepContext.stepExecution.failureExceptions.firstOrNull()
                        if (exception != null) {
                            context.stepContext.stepExecution.executionContext.put(
                                "exception",
                                exception
                            )
                        }
                    }
                })
            }
        }
    }

    @Bean
    fun karaokeNotifyNewSongStep(webhookTasklet: KaraokeWebhookTasklet): Step = batch {
        step("karaokeNotifyNewSongStep") {
            chunk<KaraokeSubscribeChannelEntity, KaraokeSubscribeChannelEntity>(1, txManager) {
                reader(webhookTasklet.asItemStreamReader())
                writer(webhookTasklet.asItemStreamWriter())

                faultTolerant {
                    retry<Throwable>()
                    retryLimit(3)

                    skip<Throwable>()
                    skipLimit(5)
                }
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

                                    cc.stepContext.stepExecution.executionContext.get(
                                        "exception",
                                        Throwable::class.java
                                    )?.let { t ->
                                        Field {
                                            name = "Exception"
                                            value = "`${t::class.qualifiedName}`"
                                            inline = false
                                        }

                                        Field {
                                            name = "Method"
                                            value =
                                                "`${t.stackTrace[0].methodName}(${t.stackTrace[0].fileName}:${t.stackTrace[0].lineNumber})`"
                                            inline = false
                                        }

                                        Field {
                                            name = "StackTrace"
                                            value = "```${t.stackTraceToString().take(1000)}```"
                                            inline = false
                                        }
                                    }

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