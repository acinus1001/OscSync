package dev.kuro9.module.front.discord.app.main.database

import dev.kuro9.multiplatform.common.network.discord.app.resources.Users
import dev.kuro9.multiplatform.common.network.discord.app.typeSafeHttpClient
import dev.kuro9.multiplatform.common.types.discord.app.api.user.UserInfoApiResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*

class NetworkUserInfoDatabase : UserInfoDatabase {
    private val client = typeSafeHttpClient()

    override suspend fun getUserInfo(): UserInfoApiResponse? {
        val httpResponse = client.get(Users())

        return when (httpResponse.status) {
            HttpStatusCode.Unauthorized -> TODO("재로그인 동작")
            HttpStatusCode.OK -> httpResponse.body<UserInfoApiResponse>()
            else -> TODO("에러 상황 핸들링")
        }
    }
}