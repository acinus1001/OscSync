package dev.kuro9.domain.karaoke.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TjNewSongResponseDto(
    val resultData: ResultData
) {

    @Serializable
    data class ResultData(
        val itemsTotalCount: Int,
        val items: List<SongInfo>
    ) {

        @Serializable
        data class SongInfo(
            val rownumber: Int,
            val pro: Int,
            val indexTitle: String,
            val indexSong: String,
            val publishdate: LocalDate,
        )
    }
}
