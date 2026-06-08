package dev.kuro9.multiplatform.common.types.discord.api

import kotlinx.serialization.Serializable

/**
 * @see <a href="https://docs.discord.com/developers/resources/guild#guild-object">Discord Guild Object</a>
 */
@Serializable
data class DiscordGuild(
    val id: String,
    val name: String,
    val icon: String?,
    val owner: Boolean,
) {
    val iconUrl: String? get() = icon?.let { "https://cdn.discordapp.com/icons/$id/$it.png" }
    val idLong: Long get() = id.toLong()
}
// 필요한거 있을때 문서보고 추가하기 바람. 너무 많다..