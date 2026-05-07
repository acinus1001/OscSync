package dev.kuro9.application.music.dto

import kotlinx.serialization.Serializable

@Serializable
data class MusicInfo(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val duration: Long,
) {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is MusicInfo -> false
            else -> this.id == other.id
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        return result
    }
}