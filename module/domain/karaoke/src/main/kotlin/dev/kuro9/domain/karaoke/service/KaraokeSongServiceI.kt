package dev.kuro9.domain.karaoke.service

import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import kotlinx.datetime.LocalDate

interface KaraokeSongServiceI {
    val supportBrand: KaraokeBrand
    fun isSupport(brand: KaraokeBrand): Boolean = brand == supportBrand

    /**
     * 외부에서 등록된 신곡 가져오기
     */
    suspend fun fetchNewReleaseSongs(): List<KaraokeSongDto>

    /**
     * db에서 등록된 신곡 불러오기
     */
    fun getNewReleaseSongs(targetDate: LocalDate): List<KaraokeSongDto>
}