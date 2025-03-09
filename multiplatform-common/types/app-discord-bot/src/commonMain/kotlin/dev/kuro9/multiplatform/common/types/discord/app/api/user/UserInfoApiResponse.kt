package dev.kuro9.multiplatform.common.types.discord.app.api.user

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoApiResponse(
    val userId: Long,
    val userName: String,
    val userAvatarUrl: String?,
)
