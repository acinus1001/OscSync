package dev.kuro9.domain.member.auth.repository

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object RefreshTokens : LongIdTable("refresh_token") {
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at")

    val member = reference(
        name = "member_id",
        refColumn = Members.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )

    init {
        uniqueIndex(member)
    }
}

class RefreshTokenEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RefreshTokenEntity>(RefreshTokens)

    var member by MemberEntity referencedOn RefreshTokens.member
    var token by RefreshTokens.token
    var expiresAt by RefreshTokens.expiresAt
    var createdAt by RefreshTokens.createdAt
}
