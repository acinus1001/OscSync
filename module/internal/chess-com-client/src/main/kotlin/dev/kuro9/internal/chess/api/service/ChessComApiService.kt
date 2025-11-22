package dev.kuro9.internal.chess.api.service

import dev.kuro9.internal.chess.api.dto.ChessComErrorObj
import dev.kuro9.internal.chess.api.dto.ChessComUser
import dev.kuro9.internal.chess.api.dto.ChessComUserStat
import dev.kuro9.internal.chess.api.exception.ChessApiFailureException
import dev.kuro9.internal.chess.api.structure.ChessComApi
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
class ChessComApiService {
    private val httpClient = httpClient {
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(Logging)
        install(Resources)

        defaultRequest {
            url {
                host = "api.chess.com"
                protocol = URLProtocol.HTTPS
            }
            contentType(ContentType.Application.Json)
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                exception as? ClientRequestException ?: throw exception
                val response = exception.response

                val errorBody = runCatching { response.body<ChessComErrorObj>() }.getOrNull() ?: throw exception

                throw ChessApiFailureException(response.status.value, errorBody.code, errorBody.message)
            }
        }

        expectSuccess = true
    }

    suspend fun getUser(userName: String): ChessComUser {
        return httpClient.get(toUserObj(userName)).body()
    }

    suspend fun getUserStat(userName: String): ChessComUserStat {
        return httpClient.get(ChessComApi.Player.User.Stats(toUserObj(userName))).body()
    }

    private fun toUserObj(userName: String): ChessComApi.Player.User {
        return ChessComApi.Player.User(userName = userName)
    }
}