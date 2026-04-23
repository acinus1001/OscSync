package dev.kuro9.internal.music.connecter.service

import dev.kuro9.internal.music.connecter.config.MusicConnecterConfig
import dev.kuro9.internal.music.connecter.dto.MusicInfoDto
import dev.kuro9.internal.music.connecter.resource.MusicClientResource
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import org.springframework.stereotype.Service

@Service
class MusicConnectService(
    private val config: MusicConnecterConfig,
) {

    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(Logging)
        install(Resources)

        expectSuccess = true

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = config.host
            }
        }
    }

    suspend fun getNowPlaying(): MusicInfoDto? {
        return httpClient.get(MusicClientResource.Music.Now).body<MusicInfoDto?>()
    }

    suspend fun getPlayQueue(): List<MusicInfoDto> {
        return httpClient.get(MusicClientResource.Music.QueueGet).body<List<MusicInfoDto>>()
    }

    suspend fun addPlayQueue(iTunesId: Long): MusicInfoDto {
        return httpClient.put(MusicClientResource.Music.QueuePut(iTunesId)).body<MusicInfoDto>()
    }

    suspend fun skipMusic() {
        httpClient.post(MusicClientResource.Music.Now.Skip)
    }

    suspend fun pauseMusic() {
        httpClient.post(MusicClientResource.Music.Now.Pause)
    }

    suspend fun resumeMusic() {
        httpClient.post(MusicClientResource.Music.Now.Resume)
    }
}