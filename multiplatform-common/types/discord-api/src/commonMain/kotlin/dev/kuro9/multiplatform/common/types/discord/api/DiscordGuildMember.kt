package dev.kuro9.multiplatform.common.types.discord.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordGuildMember(
    val user: DiscordUser? = null,
    val nick: String? = null,
    val avatar: String? = null,
    val banner: String? = null,
    val roles: List<Long>,
    @SerialName("joined_at") val joinedAt: String?,
    @SerialName("premium_since") val premiumSince: String? = null,
    val deaf: Boolean,
    val mute: Boolean,
    val flags: Int = 0,
    val pending: Boolean? = null,
    val permissions: String? = null,
    @SerialName("communication_disabled_until") val communicationDisabledUntil: String? = null,
)