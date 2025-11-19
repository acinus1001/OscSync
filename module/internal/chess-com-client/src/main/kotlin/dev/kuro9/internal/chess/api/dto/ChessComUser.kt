@file:OptIn(ExperimentalTime::class)

package dev.kuro9.internal.chess.api.dto

import dev.kuro9.multiplatform.common.serialization.serializer.instant.UnixTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class ChessComUser(
    @SerialName("player_id") val id: Long,
    @SerialName("@id") val atId: String,
    val avatar: String?,
    val url: String,
    val username: String,
    val followers: Int,
    val country: String,
    @SerialName("last_online") val lastOnline: UnixTimestamp,
    @SerialName("joined") val joined: UnixTimestamp,
    val status: String,
    val verified: Boolean,
    val league: String,
)