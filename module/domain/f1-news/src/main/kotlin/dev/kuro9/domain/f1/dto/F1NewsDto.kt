package dev.kuro9.domain.f1.dto

data class F1NewsDto(
    override val id: Long,
    override val title: String,
    override val path: String,
    override val imageUrl: String,
    override val imageAlt: String,
    val contentSummary: String,
) : F1NewsWithNoContentSummaryDto(
    id = id,
    title = title,
    path = path,
    imageUrl = imageUrl,
    imageAlt = imageAlt,
)

open class F1NewsWithNoContentSummaryDto(
    open val id: Long,
    open val title: String,
    open val path: String,
    open val imageUrl: String,
    open val imageAlt: String,
) {
    val href get() = "https://www.formula1.com$path"
}
