package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.protoBuf
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi

class MahjongApiService(serverInfo: ServerInfo) {
    @OptIn(ExperimentalSerializationApi::class)
    private val httpClient = getDefaultHttpClient(serverInfo) {
        install(ContentNegotiation) {
            serialization(ContentType.parse("application/x-protobuf"), protoBuf)
        }
        defaultRequest {
            accept(ContentType.Application.ProtoBuf)
            contentType(ContentType.Application.ProtoBuf)
        }
    }

    suspend fun getAllRecords(
        guildId: Long,
        page: Int,
        start: LocalDate? = null,
        endInclusive: LocalDate? = null,
        userId: Long? = null,
    ): MahjongPagingResult<MahjongRecord> {
        return httpClient.get("/services/mahjong/guilds/$guildId/records") {
            parameter("page", page)
            start?.let { parameter("start", it.toString()) }
            endInclusive?.let { parameter("endInclusive", it.toString()) }
            userId?.let { parameter("userId", it) }
        }.body<MahjongPagingResult<MahjongRecord>>()
    }
}
