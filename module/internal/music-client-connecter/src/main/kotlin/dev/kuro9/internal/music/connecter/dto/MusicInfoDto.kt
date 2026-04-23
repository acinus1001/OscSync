package dev.kuro9.internal.music.connecter.dto

import kotlinx.serialization.Serializable

@Serializable
data class MusicInfoDto(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val duration: Long,
)