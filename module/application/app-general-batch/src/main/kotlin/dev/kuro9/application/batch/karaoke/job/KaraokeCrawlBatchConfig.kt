package dev.kuro9.application.batch.karaoke.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class KaraokeCrawlBatchConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager
) {

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

                TODO()
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