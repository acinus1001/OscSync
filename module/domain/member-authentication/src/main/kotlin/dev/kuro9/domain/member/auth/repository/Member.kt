package dev.kuro9.domain.member.auth.repository

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object Members : LongIdTable("member") {
    val name = varchar("name", 255)
    val role = enumeration<MemberRole>("role")
    val avatarUrl = varchar("avatar_url", 4000).nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class MemberEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MemberEntity>(Members)

    var name by Members.name
    var role by Members.role
    var avatarUrl by Members.avatarUrl
    val createdAt by Members.createdAt
    var updatedAt by Members.updatedAt

    val authorities by MemberAuthorityEntity referrersOn MemberAuthorities.member
}