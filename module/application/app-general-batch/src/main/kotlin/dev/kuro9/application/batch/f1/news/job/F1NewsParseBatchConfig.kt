package dev.kuro9.application.batch.f1.news.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemProcessor
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.common.BatchErrorListener
import dev.kuro9.application.batch.common.handleFlow
import dev.kuro9.application.batch.f1.news.tasklet.F1NewsFetchTasklet
import dev.kuro9.application.batch.f1.news.tasklet.F1NewsSummaryTasklet
import dev.kuro9.application.batch.f1.news.tasklet.F1NewsWebhookTesklet
import dev.kuro9.application.batch.f1.news.tasklet.dto.F1NewsTargetWebhookDto
import dev.kuro9.application.batch.f1.news.tasklet.dto.F1NewsTaskletDto
import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class F1NewsParseBatchConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager,
    private val f1NewsFetchTasklet: F1NewsFetchTasklet,
    private val f1NewsSummaryTasklet: F1NewsSummaryTasklet,
    private val f1NewsWebhookTasklet: F1NewsWebhookTesklet,
    private val errorListener: BatchErrorListener,
) {

    @Bean
    fun f1NewsParseJob(): Job = batch {
        job("f1NewsParseJob") {
            step(f1NewsFetchStep()) {
                handleFlow {
                    stepBean("f1NewsSummaryStep") {
                        handleFlow {
                            stepBean("f1NewsWebhookStep") { handleFlow { end() } }
                        }
                    }
                }
            }
            listener(errorListener)
        }
    }

    @Bean
    fun f1NewsFetchStep(): Step = batch {
        step("f1NewsFetchStep") {
            chunk<F1NewsHtmlDto, F1NewsHtmlDto>(1, txManager) {
                reader(f1NewsFetchTasklet.asItemStreamReader())
                writer(f1NewsFetchTasklet.asItemStreamWriter())

                faultTolerant {
                    retry<Throwable>()
                    retryLimit(3)
                }

                listener(errorListener)
            }
        }
    }

    @Bean
    fun f1NewsSummaryStep(): Step = batch {
        step("f1NewsSummaryStep") {
            chunk<F1NewsTaskletDto, F1NewsTaskletDto>(1, txManager) {
                reader(f1NewsSummaryTasklet.asItemStreamReader())
                processor(f1NewsSummaryTasklet.asItemProcessor())
                writer(f1NewsSummaryTasklet.asItemStreamWriter())

                faultTolerant {
                    retry<Throwable>()
                    retryLimit(3)
                }

                listener(errorListener)
            }
        }
    }

    @Bean
    fun f1NewsWebhookStep(): Step = batch {
        step("f1NewsWebhookStep") {
            chunk<F1NewsTargetWebhookDto, F1NewsTargetWebhookDto>(10, txManager) {
                reader(f1NewsWebhookTasklet.asItemStreamReader())
                writer(f1NewsWebhookTasklet.asItemStreamWriter())

                faultTolerant {
                    retry<Throwable>()
                    retryLimit(3)
                }

                listener(errorListener)
            }
        }
    }
}