package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import org.springframework.stereotype.Service

@Service
internal class KaraokeTjNewSongService : KaraokeSongServiceI {
    override val supportBrand: KaraokeBrand = KaraokeBrand.TJ
    override suspend fun getNewReleaseSongs(): List<KaraokeSongDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getSongByNo(songNo: Int): KaraokeSongDto? {
        TODO()
    }
}