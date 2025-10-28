package dev.kuro9.domain.f1.repository.table

import dev.kuro9.domain.database.between
import dev.kuro9.domain.database.exists
import dev.kuro9.domain.f1.dto.F1NewsHtmlDto
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.date.util.toDateTimeRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Repository

@Repository
class F1NewsRepo {

    fun save(news: F1NewsHtmlDto): F1NewsEntity {
        return F1NewsEntity.new {
            this.classId = news.id
            this.title = news.title
            this.path = news.path
            this.imageUrl = news.imageUrl
            this.imageAlt = news.imageAlt
            this.contentSummary = null
            this.createdAt = LocalDateTime.now()
        }
    }

    fun updateContentSummary(news: F1NewsEntity, contentSummary: String) {
        news.contentSummary = contentSummary
    }

    fun findById(id: Long): F1NewsEntity? {
        return F1NewsEntity.findById(id)
    }

    fun existsByTitle(title: String): Boolean {
        return F1News.select(intLiteral(1))
            .where { F1News.title eq title }
            .exists()
    }

    fun findAll(
        size: Int,
        page: Int,
        dateRange: ClosedRange<LocalDate>? = null,
        desc: Boolean = true
    ): List<F1NewsEntity> {
        return findAll(
            size = size,
            page = page,
            desc = desc,
            dateRange?.let { F1News.createdAt.between(it.toDateTimeRange()) } ?: Op.TRUE,
        )
    }

    fun findAllWithNoSummary(): List<F1NewsEntity> {
        return findAll(
            size = 100,
            page = 1,
            desc = false,
            F1News.contentSummary.isNull()
        )
    }

    fun findAllWithSummary(lastNewsId: Long?): List<F1NewsEntity> {
        return findAll(
            size = 10,
            page = 1,
            desc = true,

            F1News.id.greater(lastNewsId ?: -1),
            F1News.contentSummary.isNotNull(),
        ).asReversed()
    }

    fun findAllWithSummary(
        size: Int,
        page: Int,
        dateRange: ClosedRange<LocalDate>? = null,
        desc: Boolean = true
    ): List<F1NewsEntity> {
        return findAll(
            size = size,
            page = page,
            desc = desc,

            dateRange?.let { F1News.createdAt.between(it.toDateTimeRange()) } ?: Op.TRUE,
            F1News.contentSummary.isNotNull(),
        )
    }

    private fun findAll(
        size: Int,
        page: Int,
        desc: Boolean = true,
        vararg op: Op<Boolean>,
    ): List<F1NewsEntity> {
        require(size > 0) { "size must be greater than 0" }
        require(page > 0) { "page must be greater than 0" }

        return F1NewsEntity.find(op.fold(Op.TRUE as Op<Boolean>) { acc, op2 -> acc.and(op2) })
            .orderBy(F1News.createdAt to (if (desc) SortOrder.DESC else SortOrder.ASC))
            .limit(size)
            .offset((page - 1L) * size)
            .toList()
    }
}