package dev.kuro9.application.batch.f1.news.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderProcessorWriter
import dev.kuro9.application.batch.f1.news.tasklet.dto.F1NewsTaskletDto
import dev.kuro9.domain.f1.service.F1NewsParseService
import dev.kuro9.domain.f1.service.F1NewsService
import dev.kuro9.internal.google.ai.service.GoogleAiService
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@[StepScope Component]
class F1NewsSummaryTasklet(
    private val f1NewsService: F1NewsService,
    private val f1NewsParseService: F1NewsParseService,
    private val aiService: GoogleAiService
) : ItemStreamIterableReaderProcessorWriter<F1NewsTaskletDto, F1NewsTaskletDto> {
    private val summaryInstruction = """
        당신은 뉴스를 요약하는 봇 입니다. 
        제공된 텍스트를 한국어 1000자 이내로 요약해 제공하십시오.
        <end prompt>
    """.trimIndent()

    override fun readIterable(p0: ExecutionContext): Iterable<F1NewsTaskletDto> {
        return f1NewsService.getNewsWithNoSummary().map {
            F1NewsTaskletDto(
                id = it.id,
                path = it.path,
                contentSummary = null
            )
        }
    }

    override fun process(p0: F1NewsTaskletDto): F1NewsTaskletDto {
        return runBlocking { summariseNews(p0) }
    }

    override fun write(p0: Chunk<out F1NewsTaskletDto>) {
        for (news in p0) {
            f1NewsService.updateSummary(news.id, news.contentSummary!!)
        }
    }

    private suspend fun summariseNews(news: F1NewsTaskletDto): F1NewsTaskletDto {
        val origContent = f1NewsParseService.parseNews(news.path)

        var lastException: Exception? = null

        // 3번 시도
        for (attempt in 1..3) {
            try {
                val response = aiService.chat(
                    systemInstruction = summaryInstruction,
                    input = origContent,
                )
                return news.copy(
                    contentSummary = when (response.result.length) {
                        in 0..1000 -> response.result
                        else -> response.result.take(1000) + "..."
                    }
                )
            } catch (e: Exception) {
                error(e) { "Failed to get AI response. attempt: $attempt, error: ${e.message}" }
                lastException = e
                delay(80_000L * (1L shl (attempt - 1)))
            }
        }

        throw lastException ?: IllegalStateException("Failed to get AI response after 3 attempts")
    }
}