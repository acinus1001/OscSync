package dev.kuro9.module.front.discord.app.component.user.database

import dev.kuro9.multiplatform.common.types.discord.app.api.user.UserInfoApiResponse

interface UserInfoDatabase {
    suspend fun getUserInfo(): UserInfoApiResponse?
}