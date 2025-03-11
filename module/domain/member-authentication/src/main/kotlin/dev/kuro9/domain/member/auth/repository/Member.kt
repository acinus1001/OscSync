package dev.kuro9.domain.member.auth.repository

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Members : LongIdTable("member") {
    val name = varchar("name", 255)
    val role = enumeration<MemberRole>("role")
    val avatarUrl = varchar("avatar_url", 4000).nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class MemberEntity(pk: EntityID<Long>) : LongEntity(pk) {
    companion object : LongEntityClass<MemberEntity>(Members)

    var name by Members.name; private set
    var role by Members.role; private set
    val createdAt by Members.createdAt
    var updatedAt by Members.updatedAt; private set

    fun updateName(name: String) {
        this.name = name
        this.updatedAt = LocalDateTime.now()
    }

    fun updateRole(role: MemberRole) {
        this.role = role
        this.updatedAt = LocalDateTime.now()
    }
}