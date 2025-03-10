package dev.kuro9.multiplatform.common.types.member

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoApiResponse(
    val userId: Long,
    val userName: String,
    val userAvatarUrl: String?,
)