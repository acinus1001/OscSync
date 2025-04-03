package dev.kuro9.domain.karaoke.service

import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service

@Service
class KaraokeNewSongService(
    newSongServices: List<KaraokeSongServiceI>,
) {
    private val log by useLogger()
    private val serviceMap = newSongServices.associateBy { it.supportBrand }
    private val context = Dispatchers.IO + CoroutineName("KaraokeService")

    /**
     * 브랜드별 오늘 새로 추가된 노래 가져오기
     */
    suspend fun fetchNewSongs(brand: KaraokeBrand) {
        serviceMap[brand]?.fetchNewReleaseSongs()
    }

    /**
     * 오늘 새로 추가된 노래 가져오기
     */
    suspend fun fetchNewSongs(): Deferred<List<KaraokeSongDto>> {
        return withContext(context) {
            async {
                serviceMap.values
                    .map { async(CoroutineName("KaraokeNew:${it.supportBrand}")) { it.fetchNewReleaseSongs() } }
                    .awaitAll()
                    .flatMap { it }
            }
        }
    }

    /**
     * db에 저장된 오늘 새로 추가된 노래 리턴
     */
    fun getNewReleaseSongs(brand: KaraokeBrand, targetDate: LocalDate = LocalDate.now()): List<KaraokeSongDto> {
        return serviceMap[brand]?.getNewReleaseSongs(targetDate) ?: emptyList()
    }
}