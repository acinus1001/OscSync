package dev.kuro9.domain.f1.service

import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
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

    fun getNewsWithNoSummary(): List<F1NewsEntity> {
        return repo.findAllWithNoSummary()
    }

    @Transactional
    fun save(news: F1NewsHtmlDto) {
        repo.findByClassId(news.id)?.let {
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
}