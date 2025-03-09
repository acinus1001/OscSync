package dev.kuro9.module.front.discord.app.main.database

import dev.kuro9.multiplatform.common.types.discord.app.api.user.UserInfoApiResponse

interface UserInfoDatabase {
    suspend fun getUserInfo(): UserInfoApiResponse?
}