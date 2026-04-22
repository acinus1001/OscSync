package dev.kuro9.internal.itunes.service

import dev.kuro9.internal.itunes.dto.ItunesApiResult
import dev.kuro9.internal.itunes.dto.ItunesSongSearchDto
import dev.kuro9.internal.itunes.network.ItunesApiResource
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.github.harryjhin.slf4j.extension.info
import io.github.harryjhin.slf4j.extension.warn
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import org.springframework.stereotype.Service

@Service
class ItunesApiService {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
            register(ContentType.Text.JavaScript, KotlinxSerializationConverter(minifyJson))
        }
        install(Logging)
        install(Resources)
        expectSuccess = true

        defaultRequest {
            accept(ContentType.Any)
            url {
                protocol = URLProtocol.HTTPS
                host = "itunes.apple.com"
            }
        }
    }

    suspend fun searchMusic(musicName: String): List<ItunesSongSearchDto> {
        val response = httpClient.get(ItunesApiResource.Search(musicName))
        val result = response.body<ItunesApiResult<ItunesSongSearchDto>>()

        info { "itunes api result count for $musicName: ${result.resultCount}" }
        return result.results
    }

    suspend fun getItunesSongInfo(iTunesId: Long): ItunesSongSearchDto? {
        val response = httpClient.get(ItunesApiResource.Lookup(iTunesId))
        val result = response.body<ItunesApiResult<ItunesSongSearchDto>>()

        when (result.resultCount) {
            0 -> this@ItunesApiService.info { "iTunes song not found: $iTunesId" }
            1 -> Unit
            else -> this@ItunesApiService.warn { "iTunes song duplicated: $iTunesId" }
        }

        return result.results.firstOrNull()
    }
}