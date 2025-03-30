package dev.kuro9.domain.karaoke.service

import dev.kuro9.common.logger.useLogger
import dev.kuro9.domain.karaoke.dto.KaraokeSongApiDto
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.accept
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service

@Service
class KaraokeApiService {
    private val log by useLogger()
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }

        defaultRequest {
            accept(ContentType.Application.Json)
        }

        expectSuccess = true
    }

    /**
     * 곡 넘버로 검색
     */
    suspend fun getSongInfoByNo(brand: KaraokeBrand, songNo: Int): KaraokeSongDto? {
        val response = getApiResult("no", brand, songNo.toString())
        return response
            .firstOrNull { it.no == songNo.toString() }
            ?.toNormalDto()

    }

    /**
     * 곡 이름으로 검색
     */
    suspend fun getSongInfoByName(brand: KaraokeBrand, name: String): List<KaraokeSongDto> {
        return getApiResult("song", brand, name).map { it.toNormalDto() }
    }

    /**
     * 가수 이름으로 검색
     */
    suspend fun getSongInfoByArtist(brand: KaraokeBrand, singerName: String): List<KaraokeSongDto> {
        return getApiResult("singer", brand, singerName).map { it.toNormalDto() }
    }

    private suspend fun getApiResult(type: String, brand: KaraokeBrand, queryValue: String): List<KaraokeSongApiDto> {
        return httpClient.get("https://api.manana.kr/karaoke/$type/$queryValue/${brand.queryName}.json").body()
    }

    private fun KaraokeSongApiDto.toNormalDto(): KaraokeSongDto {
        return KaraokeSongDto(
            brand = KaraokeBrand.parse(brand),
            songNo = no.toInt(),
            title = title,
            singer = singer,
            releaseDate = runCatching { LocalDate.parse(release) }.getOrElse { LocalDate(1970, 1, 1) },
        )
    }
}