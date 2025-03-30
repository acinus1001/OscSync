package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand

interface KaraokeSongServiceI {
    val supportBrand: KaraokeBrand
    fun isSupport(brand: KaraokeBrand): Boolean = brand == supportBrand
    suspend fun getNewReleaseSongs(): List<KaraokeSongDto>
}