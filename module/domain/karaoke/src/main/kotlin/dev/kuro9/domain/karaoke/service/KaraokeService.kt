package dev.kuro9.domain.karaoke.service

import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class KaraokeService(
    newSongServices: List<KaraokeSongServiceI>
) {
    private val log by useLogger()
    private val serviceMap = newSongServices.associateBy { it.supportBrand }
    private val context = Dispatchers.IO + CoroutineName("KaraokeService")

    suspend fun getNewSongs(brand: KaraokeBrand): List<KaraokeSongDto> {
        return serviceMap[brand]
            ?.getNewReleaseSongs()
            ?: emptyList()
    }

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

    suspend fun getSongByNo(brand: KaraokeBrand, songNo: Int): KaraokeSongDto? {
        return serviceMap[brand]?.getSongByNo(songNo)
    }
}