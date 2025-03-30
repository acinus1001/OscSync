package dev.kuro9.domain.karaoke.service

import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class KaraokeNewSongService(
    newSongServices: List<KaraokeSongServiceI>,
) {
    private val log by useLogger()
    private val serviceMap = newSongServices.associateBy { it.supportBrand }
    private val context = Dispatchers.IO + CoroutineName("KaraokeService")

    /**
     * 브랜드별 오늘 새로 추가된 노래 리턴
     */
    suspend fun getNewSongs(brand: KaraokeBrand): List<KaraokeSongDto> {
        return serviceMap[brand]
            ?.getNewReleaseSongs()
            ?: emptyList()
    }

    /**
     * 오늘 새로 추가된 노래 리턴
     */
    suspend fun getNewSongs(): Deferred<List<KaraokeSongDto>> {
        return withContext(context) {
            async {
                serviceMap.values
                    .map { async(CoroutineName("KaraokeNew:${it.supportBrand}")) { it.getNewReleaseSongs() } }
                    .awaitAll()
                    .flatMap { it }
            }
        }
    }
}