package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeSongFetchTasklet
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeWebhookTasklet
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.scope.context.ChunkContext
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
                    stepBean("batchFailureNotifyStep")
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
}