package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.KaraokeRepo
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongEntity
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class KaraokeTjNewSongService(
    private val karaokeRepo: KaraokeRepo,
) : KaraokeSongServiceI {
    override val supportBrand: KaraokeBrand = KaraokeBrand.TJ
    override suspend fun getNewReleaseSongs(): List<KaraokeSongDto> {
        val requestDate = LocalDate.now()
        // db 체크
        karaokeRepo.findByReleaseDate(
            brand = KaraokeBrand.TJ,
            releaseDateRange = requestDate..requestDate,
        )
            .takeIf { it.isNotEmpty() }
            ?.map { it.toDto() }
            ?.run { return this }

        // 없으면 크롤링
        val document = Jsoup.connect("https://www.tjmedia.com/tjsong/song_monthnew.asp").get()
        val result = document.getElementsByClass("board_type1")
            .single().getElementsByTag("tbody")
            .single().children()
            .drop(1)
            .map {
                val (
                    songNo,
                    songName,
                    singer,
                ) = it.getElementsByTag("td").map { td -> td.text() }

                KaraokeSongDto(
                    brand = KaraokeBrand.TJ,
                    songNo = songNo.toInt(),
                    title = songName,
                    singer = singer,
                    releaseDate = requestDate
                )
            }

        // db 저장
        CoroutineScope(currentCoroutineContext()).launch {
            result.forEach { song ->
                karaokeRepo.insertKaraokeSong(
                    brand = song.brand,
                    songNo = song.songNo,
                    title = song.title,
                    singer = song.singer,
                    releaseDate = song.releaseDate,
                )
            }
        }

        return result
    }

    private fun KaraokeSongEntity.toDto() = KaraokeSongDto(
        brand = brand.value,
        songNo = songNo.value,
        title = title,
        singer = singer,
        releaseDate = releaseDate,
    )
}