package dev.kuro9.domain.f1.dto

data class F1NewsDto(
    val id: Long,
    val title: String,
    val href: String,
    val imageUrl: String,
    val imageAlt: String,
    val contentSummary: String,
)
