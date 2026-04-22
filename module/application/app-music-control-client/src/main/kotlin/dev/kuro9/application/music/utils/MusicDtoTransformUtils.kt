package dev.kuro9.application.music.utils

import dev.kuro9.application.music.dto.MusicInfo
import dev.kuro9.internal.itunes.dto.ItunesSongSearchDto

internal fun ItunesSongSearchDto.toMusicInfo(): MusicInfo = MusicInfo(
    id = trackId,
    title = trackName,
    artist = artistName,
    album = collectionName,
    imageUrl = artworkUrl100,
    duration = trackTimeMillis
)