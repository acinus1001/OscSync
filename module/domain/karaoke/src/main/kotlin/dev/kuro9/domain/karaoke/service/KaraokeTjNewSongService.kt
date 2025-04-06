package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.KaraokeRepo
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongEntity
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class KaraokeTjNewSongService(
    private val karaokeRepo: KaraokeRepo,
) : KaraokeSongServiceI {
    override val supportBrand: KaraokeBrand = KaraokeBrand.TJ
    override suspend fun fetchNewReleaseSongs(): List<KaraokeSongDto> {
        val requestDate = LocalDate.now()

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
            .filter { it.songNo in 52565..53000 }

        return result

//        BatchInsertStatement(KaraokeSongs).apply {
//            result.forEach { song ->
//                karaokeRepo.insertKaraokeSong(
//                    brand = song.brand,
//                    songNo = song.songNo,
//                    title = song.title,
//                    singer = song.singer,
//                    releaseDate = song.releaseDate,
//                )
//            }
//        }.execute(TransactionManager.current())
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
}