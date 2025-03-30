package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.karaoke.service.KaraokeNewSongService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class KaraokeCrawlBatchConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager,
    private val karaokeService: KaraokeNewSongService,
) {
    private val log by useLogger()

    @Bean
    fun karaokeCrawlJob(): Job = batch {
        job("karaokeCrawlJob") {
            step(karaokeCrawlNewSongStep()) {
                on(ExitStatus.COMPLETED.exitCode) {
                    step(karaokeNotifyNewSongStep())
                }
                on(ExitStatus.FAILED.exitCode) {
                    step(karaokeNotifyCrawlFailure())
                }
                on("*") {
                    stop()
                }
            }
        }
    }

    @Bean
    fun karaokeCrawlNewSongStep(): Step = batch {
        step("karaokeCrawlNewSongStep") {
            tasklet(transactionManager = txManager, tasklet = { sc: StepContribution, cc: ChunkContext ->
                val throwable = try {
                    runBlocking {
                        karaokeService.fetchAndSaveNewSongs().await()
                    }
                    null
                } catch (t: Throwable) {
                    log.error("Exception on crawl new songs", t)
                    t
                }

                when (throwable) {
                    null -> RepeatStatus.FINISHED

                    else -> {
                        sc.exitStatus = ExitStatus.FAILED
                        RepeatStatus.CONTINUABLE
                    }
                }
            })
        }
    }

    @Bean
    fun karaokeNotifyNewSongStep(): Step = batch {
        step("karaokeNotifyNewSongStep") {
            tasklet(transactionManager = txManager, tasklet = { sc: StepContribution, cc: ChunkContext ->

                TODO()
            })
        }
    }

    @Bean
    fun karaokeNotifyCrawlFailure(): Step = batch {
        step("karaokeNotifyNewSongStep") {
            tasklet(transactionManager = txManager, tasklet = { sc: StepContribution, cc: ChunkContext ->

                TODO()
            })
        }
    }
}