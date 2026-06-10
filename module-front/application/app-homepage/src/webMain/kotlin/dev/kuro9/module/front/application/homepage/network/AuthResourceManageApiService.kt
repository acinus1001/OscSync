package dev.kuro9.module.front.application.homepage.network

import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.application.homepage.utils.getDefaultHttpClient
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.app.homepage.common.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthResourceManageApiService(serverInfo: ServerInfo, tokenRefreshService: TokenRefreshService) {
    private val httpClient = getDefaultHttpClient(serverInfo, tokenRefreshService)

    suspend fun getStringResourceList(): StringResourceListResponse {
        return httpClient.get("/resources/admin/strings").body()
    }

    suspend fun getStringResourceInfo(id: String): StringResourceResponse {
        return httpClient.get("/resources/admin/strings") {
            url {
                appendPathSegments(id)
            }
        }.body()
    }

    suspend fun postNewStringResource(
        string: String,
        allowedAuthorities: List<String>,
        description: String? = null,
    ): String {
        val createdResourceId = httpClient.post("/resources/admin/strings") {
            contentType(ContentType.Application.Json)
            setBody(
                StringResourcePostRequest(
                    string = string,
                    description = description,
                    allowed = allowedAuthorities,
                )
            )
        }.bodyAsText()

        return createdResourceId
    }

    suspend fun modifyStringResource(
        id: String,
        string: String? = null,
        allowedAuthorities: List<String>? = null,
        description: String? = null,
    ) {
        httpClient.patch("/resources/admin/strings") {
            url {
                appendPathSegments(id)
            }
            contentType(ContentType.Application.Json)
            setBody(
                StringResourceModifyRequest(
                    string = string,
                    description = description,
                    allowed = allowedAuthorities,
                )
            )
        }
    }

    suspend fun getImageResourceList(): ImageResourceListResponse {
        return httpClient.get("/resources/admin/images").body()
    }

    suspend fun getImageResourceInfo(id: String): ImageResourceResponse {
        return httpClient.get("/resources/admin/images") {
            url {
                appendPathSegments(id)
            }
        }.body()
    }

    suspend fun postNewImageResource(
        image: ByteArray,
        contentType: String,
        allowedAuthorities: List<String>,
        description: String? = null,
    ): String {
        check(ContentType.Image.contains(contentType)) { "contentType must be image" }
        val createdResourceId = httpClient.submitFormWithBinaryData(
            url = "/resources/admin/images",
            formData = formData {
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, ContentType.parse(contentType))
                    append(HttpHeaders.ContentDisposition, "filename=\"upload.png\"")
                })
                append(
                    key = "body",
                    value = minifyJson.encodeToString(
                        ImageResourcePostRequest(
                            description = description,
                            allowed = allowedAuthorities,
                        )
                    ),
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    },
                )
            }
        ).bodyAsText()

        return createdResourceId
    }

    suspend fun modifyImageResource(
        id: String,
        allowedAuthorities: List<String>? = null,
        description: String? = null,
    ) {
        httpClient.patch("/resources/admin/images") {
            url {
                appendPathSegments(id)
            }
            contentType(ContentType.Application.Json)
            setBody(
                ImageResourceModifyRequest(
                    description = description,
                    allowed = allowedAuthorities,
                )
            )
        }
    }
}