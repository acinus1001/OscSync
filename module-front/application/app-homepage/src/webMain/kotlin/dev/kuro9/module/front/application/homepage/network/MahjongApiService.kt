package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.serialization.protoBuf
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongDetailRecord
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi

class MahjongApiService(
    serverInfo: ServerInfo,
    tokenRefreshService: TokenRefreshService,
    userViewModel: UserViewModel
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val httpClient =
        getDefaultHttpClient(serverInfo, tokenRefreshService, userViewModel) {
            installOrReplace(ContentNegotiation) {
                serialization(ContentType.parse("application/x-protobuf"), protoBuf)
                json(minifyJson)
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

    suspend fun getRecord(
        guildId: Long,
        recordId: Long,
    ): MahjongDetailRecord {
        return httpClient.get("/services/mahjong/guilds/$guildId/records/$recordId")
            .body<MahjongDetailRecord>()
    }

    suspend fun getGuildStat(
        guildId: Long,
    ): dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongGuildStat {
        return httpClient.get("/services/mahjong/guilds/$guildId/stats")
            .body<dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongGuildStat>()
    }

    suspend fun getUserStat(
        guildId: Long,
        userId: Long,
    ): dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat {
        return httpClient.get("/services/mahjong/guilds/$guildId/stats/$userId")
            .body<dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat>()
    }

    suspend fun getUserStatByYearMonth(
        guildId: Long,
        userId: Long,
        yearMonth: String,
    ): dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat {
        return httpClient.get("/services/mahjong/guilds/$guildId/stats/$userId/yearmonth/$yearMonth")
            .body<dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat>()
    }
}
