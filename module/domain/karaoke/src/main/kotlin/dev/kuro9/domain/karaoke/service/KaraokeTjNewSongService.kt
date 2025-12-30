package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.dto.TjNewSongResponseDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.KaraokeRepo
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongEntity
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class KaraokeTjNewSongService(
    private val karaokeRepo: KaraokeRepo,
) : KaraokeSongServiceI {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(Logging)
        expectSuccess = true
    }
    override val supportBrand: KaraokeBrand = KaraokeBrand.TJ
    override suspend fun fetchNewReleaseSongs(): List<KaraokeSongDto> {
        val requestYearMonth = YearMonth.now()
        val basicIsoYearMonth = "${requestYearMonth.year}${requestYearMonth.monthValue.toString().padStart(2, '0')}"
        val response = httpClient.submitForm(
            url = "https://www.tjmedia.com/legacy/api/newSongOfMonth",
            formParameters = parameters {
                append("searchYm", basicIsoYearMonth)
            }
        )

        val songs = response.body<TjNewSongResponseDto>()

        return songs.resultData.items
            .filter { it.pro in 52894..53000 || it.pro in 52400..52499 || it.indexTitle.isJapanese() }
            .map {
                KaraokeSongDto(
                    brand = supportBrand,
                    songNo = it.pro,
                    title = it.indexTitle,
                    singer = it.indexSong,
                    releaseDate = it.publishdate,
                )
            }
    }

    override fun getNewReleaseSongs(targetDate: LocalDate): List<KaraokeSongDto> {
        return karaokeRepo.findByReleaseDate(
            brand = KaraokeBrand.TJ,
            releaseDateRange = targetDate..targetDate,
        )
            .takeIf { it.isNotEmpty() }
            ?.map { it.toDto() }
            ?: emptyList()
    }

    private fun KaraokeSongEntity.toDto() = KaraokeSongDto(
        brand = brand.value,
        songNo = songNo.value,
        title = title,
        singer = singer,
        releaseDate = releaseDate,
    )

    private fun String.isJapanese(): Boolean {
        // 히라가나
        if (this.any { it in '\u3041'..'\u3096' }) return true
        // 가타카나
        if (this.any { it in '\u30a0'..'\u30ff' }) return true

        return false
    }
}