package dev.kuro9.module.front.internal.member.service

import dev.kuro9.module.front.internal.member.exception.MemberApiException
import dev.kuro9.module.front.internal.member.resource.MemberApiResource
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class MemberApiService(
    private val host: String,
    private val port: Int,
) {
    private val httpClient = httpClient {
        install(Logging)
        install(Resources)
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(ContentNegotiation) {
            json(minifyJson)
        }
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { cause: Throwable, request: HttpRequest ->
                when (cause) {
                    is ClientRequestException -> {
                        when (cause.response.status) {
                            HttpStatusCode.Unauthorized -> throw MemberApiException.Unauthorized(cause)
                            HttpStatusCode.Forbidden -> throw MemberApiException.Forbidden(cause)
                            HttpStatusCode.NotFound -> throw MemberApiException.NotFound(cause)
                            else -> Unit
                        }
                    }
//                    is ServerResponseException -> Unit
                }
            }
        }

        defaultRequest {
            url {
                host = this@MemberApiService.host
                port = this@MemberApiService.port
                protocol = if (host == "localhost") URLProtocol.HTTP else URLProtocol.HTTPS
            }
            contentType(ContentType.Application.Json)
        }

        engine {
        }

        expectSuccess = true
    }

    @Throws(MemberApiException::class)
    suspend fun getMyInfo(): UserInfoApiResponse {
        return httpClient.get(MemberApiResource.Me()).body()
    }

    @Throws(MemberApiException::class)
    suspend fun logout() {
        httpClient.post(MemberApiResource.Logout())
    }
}