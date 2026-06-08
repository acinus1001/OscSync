package dev.kuro9.domain.member.auth.repository

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.crypt.Algorithms
import org.jetbrains.exposed.v1.crypt.encryptedVarchar
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.datetime

object DiscordOAuthTokens : LongIdTable("discord_oauth_token") {
    val member = reference(
        name = "member_id",
        refColumn = Members.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    ).uniqueIndex()

    val accessToken = encryptedVarchar(
        name = "access_token",
        cipherTextLength = 4096,
        encryptor = Algorithms.AES_256_PBE_GCM(
            password = System.getenv("COL_AES_KEY"),
            salt = System.getenv("COL_AES_SALT"),
        )
    )
    val refreshToken = encryptedVarchar(
        name = "refresh_token",
        cipherTextLength = 4096,
        encryptor = Algorithms.AES_256_PBE_GCM(
            password = System.getenv("COL_AES_KEY"),
            salt = System.getenv("COL_AES_SALT"),
        )
    )
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class DiscordOAuthTokenEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DiscordOAuthTokenEntity>(DiscordOAuthTokens)

    var member by MemberEntity referencedOn DiscordOAuthTokens.member
    var accessToken by DiscordOAuthTokens.accessToken
    var refreshToken by DiscordOAuthTokens.refreshToken
    var expiresAt by DiscordOAuthTokens.expiresAt
    var createdAt by DiscordOAuthTokens.createdAt
    var updatedAt by DiscordOAuthTokens.updatedAt
}