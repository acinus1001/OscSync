package dev.kuro9.domain.f1.service

import dev.kuro9.domain.f1.dto.F1NewsDto
import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
import dev.kuro9.domain.f1.dto.F1NewsWithNoContentSummaryDto
import dev.kuro9.domain.f1.repository.table.F1NewsEntity
import dev.kuro9.domain.f1.repository.table.F1NewsRepo
import io.github.harryjhin.slf4j.extension.info
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class F1NewsService(private val repo: F1NewsRepo) {

    fun getNewsById(id: Long): F1NewsEntity? {
        val news = repo.findById(id)
        return news
    }

    fun getNewsWithNoSummary(): List<F1NewsWithNoContentSummaryDto> {
        return repo.findAllWithNoSummary().map { it.toNoSummaryDto() }
    }

    fun getNewsWithSummary(
        size: Int,
        page: Int,
    ): List<F1NewsDto> {
        return repo.findAllWithSummary(size, page).map { it.toDto() }
    }

    fun getAllNewsWithSummary(
        lastNewsId: Long?
    ): List<F1NewsDto> {
        return repo.findAllWithSummary(lastNewsId).map { it.toDto() }
    }

    @Transactional
    fun save(news: F1NewsHtmlDto) {
        val isExists = repo.existsByTitle(news.title)
        if (isExists) {
            info { "news already exists: ${news.title}" }
            return
        }

        repo.save(news)
    }

    @Transactional
    fun updateSummary(id: Long, summary: String) {
        val entity = repo.findById(id) ?: return
        repo.updateContentSummary(entity, summary)
    }

    private fun F1NewsEntity.toDto(): F1NewsDto {
        return F1NewsDto(
            id = id.value,
            title = title,
            path = path,
            imageUrl = imageUrl,
            imageAlt = imageAlt,
            contentSummary = contentSummary!!,
        )
    }

    private fun F1NewsEntity.toNoSummaryDto(): F1NewsWithNoContentSummaryDto {
        return F1NewsWithNoContentSummaryDto(
            id = id.value,
            title = title,
            path = path,
            imageUrl = imageUrl,
            imageAlt = imageAlt,
        )
    }
}