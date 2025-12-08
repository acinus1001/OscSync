package dev.kuro9.application.batch.common

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import dev.kuro9.application.batch.discord.config.DiscordProperties
import dev.kuro9.application.batch.discord.dto.DiscordWebhookPayload
import dev.kuro9.application.batch.discord.dto.Embed
import dev.kuro9.application.batch.discord.service.DiscordWebhookService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchErrorNotifyStep(private val batch: BatchDsl, private val txManager: PlatformTransactionManager) {

    @Bean
    fun batchFailureNotifyStep(
        webhookService: DiscordWebhookService,
        discordProperties: DiscordProperties,
        errorContext: BatchErrorContext,
    ): Step = batch {
        step("batchFailureNotifyStep") {
            tasklet(transactionManager = txManager, tasklet = { sc: StepContribution, cc: ChunkContext ->
                runBlocking {
                    webhookService.sendWebhook(
                        discordProperties.errorUrl, payload = DiscordWebhookPayload(
                            username = "Batch Error Notify",
                            embeds = listOf(
                                Embed {
                                    title = "Batch Error Alert"
                                    description = sc.stepExecution.stepName
                                    color = 0xFF0000

                                    Field {
                                        name = "Batch ID"
                                        value = "`${sc.stepExecution.jobExecution.id}`"
                                    }

                                    Field {
                                        name = "Step Name"
                                        value = "`${
                                            sc.stepExecution.jobExecution.stepExecutions.reversed()
                                                .getOrNull(1)?.stepName ?: "Unknown Step"
                                        }`"
                                    }

                                    errorContext.exception?.let { t ->
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