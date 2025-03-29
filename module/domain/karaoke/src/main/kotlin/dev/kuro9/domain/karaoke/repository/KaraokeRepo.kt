package dev.kuro9.domain.karaoke.repository

import dev.kuro9.domain.database.between
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongEntity
import dev.kuro9.domain.karaoke.repository.table.KaraokeSongs
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class KaraokeRepo {

    @Transactional
    fun insertKaraokeSong(
        brand: KaraokeBrand,
        songNo: Int,
        title: String,
        artist: String,
        releaseDate: LocalDate = LocalDate.now()
    ) {
        KaraokeSongs.insertIgnore {
            it[KaraokeSongs.brand] = brand
            it[KaraokeSongs.songNo] = songNo
            it[KaraokeSongs.title] = title
            it[KaraokeSongs.artist] = artist
            it[KaraokeSongs.releaseDate] = releaseDate
            it[KaraokeSongs.createdAt] = LocalDateTime.now()
        }
    }

    fun findByReleaseDate(
        brand: KaraokeBrand,
        releaseDateRange: ClosedRange<LocalDate>,
    ): List<KaraokeSongEntity> {
        return Op.build { KaraokeSongs.brand eq brand }
            .and { KaraokeSongs.releaseDate between releaseDateRange }
            .let(KaraokeSongEntity::find)
            .toList()
    }
}