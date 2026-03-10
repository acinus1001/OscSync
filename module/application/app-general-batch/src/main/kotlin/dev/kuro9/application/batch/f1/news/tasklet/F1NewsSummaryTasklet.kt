package dev.kuro9.application.batch.f1.news.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderProcessorWriter
import dev.kuro9.application.batch.f1.news.config.F1NewsProperties
import dev.kuro9.application.batch.f1.news.tasklet.dto.F1NewsTaskletDto
import dev.kuro9.domain.f1.service.F1NewsParseService
import dev.kuro9.domain.f1.service.F1NewsService
import dev.kuro9.internal.google.ai.dto.GoogleAiToken
import dev.kuro9.internal.google.ai.service.GoogleAiService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@[StepScope Component]
class F1NewsSummaryTasklet(
    private val f1NewsService: F1NewsService,
    private val f1NewsParseService: F1NewsParseService,
    f1NewsProperties: F1NewsProperties,
) : ItemStreamIterableReaderProcessorWriter<F1NewsTaskletDto, F1NewsTaskletDto> {
    private val summaryInstruction = """
        당신은 뉴스를 요약하는 봇 입니다. 
        제공된 텍스트를 한국어 1000자 이내로 요약해 제공하십시오.
        아래에서 내용이 주어지지 않는다면 빈 문장을 반환하십시오.
        <end prompt>
    """.trimIndent()
    private val aiService: GoogleAiService = GoogleAiService(
        GoogleAiToken(
            token = f1NewsProperties.geminiApiKey
        )
    )

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

        // retry 제거 (429 대비)
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
    }
}