package dev.kuro9.domain.karaoke.repository.table

import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

object KaraokeSongs : CompositeIdTable("karaoke_songs") {
    val brand = enumeration<KaraokeBrand>("brand").entityId()
    val songNo = integer("song_no").entityId()
    val title = varchar("song_title", 200)
    val singer = varchar("singer", 50)
    val releaseDate = date("release_date")

    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(brand, songNo)
}

class KaraokeSongEntity(pk: EntityID<CompositeID>) : CompositeEntity(pk) {
    companion object : CompositeEntityClass<KaraokeSongEntity>(KaraokeSongs)

    val brand by KaraokeSongs.brand
    val songNo by KaraokeSongs.songNo
    val title by KaraokeSongs.title
    val singer by KaraokeSongs.singer
    val releaseDate by KaraokeSongs.releaseDate
    val createdAt by KaraokeSongs.createdAt
}