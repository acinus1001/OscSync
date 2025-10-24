package dev.kuro9.domain.karaoke.repository

import dev.kuro9.domain.database.between
import dev.kuro9.domain.karaoke.dto.KaraokeSongDto
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongEntity
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongs
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.springframework.stereotype.Repository

@Repository
class KaraokeRepo {

    fun insertKaraokeSong(
        brand: KaraokeBrand,
        songNo: Int,
        title: String,
        singer: String,
        releaseDate: LocalDate = LocalDate.now()
    ) {
        KaraokeSongs.insertIgnore {
            it[KaraokeSongs.brand] = brand
            it[KaraokeSongs.songNo] = songNo
            it[KaraokeSongs.title] = title
            it[KaraokeSongs.singer] = singer
            it[KaraokeSongs.releaseDate] = releaseDate
            it[KaraokeSongs.createdAt] = LocalDateTime.now()
        }
    }

    fun batchInsertSongs(songs: Iterable<KaraokeSongDto>) {
        KaraokeSongs.batchInsert(songs, ignore = true) {
            this[KaraokeSongs.brand] = it.brand
            this[KaraokeSongs.songNo] = it.songNo
            this[KaraokeSongs.title] = it.title
            this[KaraokeSongs.singer] = it.singer
            this[KaraokeSongs.releaseDate] = it.releaseDate
            this[KaraokeSongs.createdAt] = LocalDateTime.now()
        }
    }

    fun findByReleaseDate(
        brand: KaraokeBrand,
        releaseDateRange: ClosedRange<LocalDate>,
    ): List<KaraokeSongEntity> {
        return (KaraokeSongs.brand eq brand)
            .and { KaraokeSongs.releaseDate between releaseDateRange }
            .let(KaraokeSongEntity::find)
            .toList()
    }
}