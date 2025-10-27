package dev.kuro9.domain.f1.repository.table

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime


object F1News : LongIdTable("f1_news") {
    val classId = varchar("class_id", 100)
    val title = varchar("title", 500)
    val path = varchar("path", 500)
    val imageUrl = varchar("image_url", 1000)
    val imageAlt = varchar("image_alt", 200)
    val contentSummary = text("content_summary").nullable()
    val createdAt = datetime("created_at")

    init {
        index(isUnique = false, classId) // 신뢰하기는 좀 그래서 non-unique로..
        index(isUnique = false, createdAt)
    }
}

class F1NewsEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<F1NewsEntity>(F1News)

    var classId by F1News.classId; internal set
    var title by F1News.title; internal set
    var path by F1News.path; internal set
    var imageUrl by F1News.imageUrl; internal set
    var imageAlt by F1News.imageAlt; internal set
    var contentSummary by F1News.contentSummary; internal set
    var createdAt by F1News.createdAt; internal set
}