package dev.kuro9.application.music.dto

data class MusicInfo(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val duration: Long,
)