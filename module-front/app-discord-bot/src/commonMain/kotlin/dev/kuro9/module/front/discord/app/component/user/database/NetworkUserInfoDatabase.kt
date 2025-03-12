package dev.kuro9.module.front.discord.app.component.user.database

import com.arkivanov.mvikotlin.logging.logger.DefaultLogger
import dev.kuro9.multiplatform.common.network.discord.app.resources.Users
import dev.kuro9.multiplatform.common.network.discord.app.typeSafeHttpClient
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.util.logging.*

class NetworkUserInfoDatabase : UserInfoDatabase {
    private val client = typeSafeHttpClient()
    private val logger = KtorSimpleLogger(this::class.simpleName!!)

    override suspend fun getUserInfo(): UserInfoApiResponse? {
        val httpResponse = runCatching {
            client.get(Users())
        }.onFailure {
            logger.error("Error in getting user info", it)
        }.getOrThrow()

        return when (httpResponse.status) {
            HttpStatusCode.Unauthorized -> {
                DefaultLogger.log("401")
                throw NotImplementedError("401 not implemented")
            }

            HttpStatusCode.OK -> httpResponse.body<UserInfoApiResponse>()
            else -> {
                DefaultLogger.log(httpResponse.toString())
                throw NotImplementedError("${httpResponse.status}")
            }
        }
    }

    override suspend fun deleteUserInfo() {
        runCatching {
            client.post(Users.Logout())
        }.onFailure {
            logger.error("Error on logout", it)
        }.getOrThrow()
    }
}