@file:OptIn(ExperimentalUuidApi::class)

package dev.kuro9.application.homepage.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object HomepageAuthResources : LongIdTable("homepage_auth_resource") {
    val description = varchar("description", 255).nullable()
    val type = varchar("type", 50)
    val externalId = uuid("external_id").uniqueIndex().clientDefault { Uuid.random() }
    val allowed = array<String>("allowed")
    val resource = blob("resource")

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class HomepageAuthResourceEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<HomepageAuthResourceEntity>(HomepageAuthResources)

    val description by HomepageAuthResources.description
    val type by HomepageAuthResources.type
    val externalId by HomepageAuthResources.externalId
    val allowed by HomepageAuthResources.allowed
    val resource by HomepageAuthResources.resource
    val createdAt by HomepageAuthResources.createdAt
    val updatedAt by HomepageAuthResources.updatedAt
}