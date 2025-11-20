package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.common.BatchErrorListener
import dev.kuro9.application.batch.common.handleFlow
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeSongFetchTasklet
import dev.kuro9.application.batch.karaoke.tasklet.KaraokeWebhookTasklet
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.repository.table.KaraokeSubscribeChannelEntity
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class KaraokeCrawlBatchConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager,
    private val errorListener: BatchErrorListener,
    private val fetchTasklet: KaraokeSongFetchTasklet,
    private val webhookTasklet: KaraokeWebhookTasklet,
) {

    @Bean
    fun karaokeCrawlJob(): Job = batch {
        job("karaokeCrawlJob") {
            step(karaokeCrawlNewSongStep()) {
                handleFlow {
                    step(karaokeNotifyNewSongStep()) {
                        handleFlow { end() }
                    }
                }
            }
            listener(errorListener)
        }
    }

    @Bean
    fun karaokeCrawlNewSongStep(): Step = batch {
        step("karaokeCrawlNewSongStep") {
            allowStartIfComplete(true)
            chunk<KaraokeSongDto, KaraokeSongDto>(1000, txManager) {
                reader(fetchTasklet.asItemStreamReader())
                writer(fetchTasklet.asItemStreamWriter())

                listener(errorListener)
            }
        }
    }

    @Bean
    fun karaokeNotifyNewSongStep(): Step = batch {
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

                listener(errorListener)
            }
        }
    }
}