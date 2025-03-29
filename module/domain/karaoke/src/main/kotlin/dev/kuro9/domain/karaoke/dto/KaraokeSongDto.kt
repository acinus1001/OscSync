package dev.kuro9.domain.karaoke.dto

import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import kotlinx.datetime.LocalDate

data class KaraokeSongDto(
    val brand: KaraokeBrand,
    val songNo: Int,
    val title: String,
    val artist: String,
    val releaseDate: LocalDate,
)
