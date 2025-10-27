package dev.kuro9.application.batch.f1.news.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderWriter
import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
import dev.kuro9.domain.f1.service.F1NewsParseService
import dev.kuro9.domain.f1.service.F1NewsService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@[StepScope Component]
class F1NewsFetchTasklet(
    private val f1ParseService: F1NewsParseService,
    private val f1Service: F1NewsService,
    @Value("#{jobParameters['executeDate']}") private val _executeDate: LocalDate,
) : ItemStreamIterableReaderWriter<F1NewsHtmlDto> {

    override fun readIterable(p0: ExecutionContext): Iterable<F1NewsHtmlDto> {
        return runBlocking { f1ParseService.parseLatestNews().asReversed() }
    }

    override fun write(p0: Chunk<out F1NewsHtmlDto>) {
        for (news in p0) {
            f1Service.save(news)
        }
    }
}