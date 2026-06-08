package dev.kuro9.domain.member.auth.repository

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object MemberAuthorities : LongIdTable("member_authority") {
    val authority = varchar("authority", 128)
    val createdAt = datetime("created_at")

    val member = reference(
        name = "member_id",
        refColumn = Members.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )

    init {
        uniqueIndex(member, authority)
    }
}

class MemberAuthorityEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MemberAuthorityEntity>(MemberAuthorities)

    var authority by MemberAuthorities.authority
    var createdAt by MemberAuthorities.createdAt

    var member by MemberEntity referencedOn MemberAuthorities.member
}