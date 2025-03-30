package dev.kuro9.domain.karaoke.dto

import kotlinx.serialization.Serializable

@Serializable
data class KaraokeSongApiDto(
    val brand: String,
    val no: String,
    val title: String,
    val singer: String,
    val release: String,
)
