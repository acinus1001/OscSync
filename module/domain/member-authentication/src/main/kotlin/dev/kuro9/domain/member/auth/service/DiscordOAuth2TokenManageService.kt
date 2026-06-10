package dev.kuro9.domain.member.auth.service

import dev.kuro9.domain.member.auth.network.DiscordOAuthApiService
import dev.kuro9.domain.member.auth.repository.DiscordOAuthTokenEntity
import dev.kuro9.domain.member.auth.repository.DiscordOAuthTokens
import dev.kuro9.multiplatform.common.date.util.now
import io.github.harryjhin.slf4j.extension.info
import io.github.harryjhin.slf4j.extension.warn
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DiscordOAuth2TokenManageService(
    private val apiService: DiscordOAuthApiService,
) {
    fun getToken(userId: Long): DiscordOAuthTokenEntity? {
        val token = DiscordOAuthTokens.selectAll()
            .where { DiscordOAuthTokens.member eq userId }
            .limit(1)
            .singleOrNull()
            ?.let { DiscordOAuthTokenEntity.wrapRow(it) } ?: return null
        if (token.expiresAt <= LocalDateTime.now()) {
            return refreshToken(userId)
        }

        info { "token id=${token.id.value}" }

        return token
    }

    fun saveToken(
        userId: Long,
        accessToken: String,
        refreshToken: String,
        expiresAt: LocalDateTime,
    ): Unit {
        val exists = getUserTokenEntity(userId)
        if (exists != null) {
            // revoke token
            info { "revoking token id=${exists.id.value}" }
            revokeToken(userId, exists)
        }

        info { "saving token" }
        DiscordOAuthTokens.upsert {
            it[this.member] = userId
            it[this.accessToken] = accessToken
            it[this.refreshToken] = refreshToken
            it[this.expiresAt] = expiresAt
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    fun refreshToken(
        userId: Long,
    ): DiscordOAuthTokenEntity? {
        val exists = getUserTokenEntity(userId)
            ?: return null

        val response = try {
            runBlocking {
                apiService.refreshToken(exists.refreshToken)
            }
        } catch (e: Exception) {
            warn(e) { "refresh token failed" }
            revokeToken(userId, exists)
            exists.delete()
            return null
        }

        exists.accessToken = response.accessToken
        exists.refreshToken = response.refreshToken
        exists.expiresAt = response.expiresAt
        exists.updatedAt = LocalDateTime.now()

        exists.flush()

        return exists
    }

    fun revokeToken(userId: Long, entity: DiscordOAuthTokenEntity? = null): Unit {
        val exists = entity ?: getUserTokenEntity(userId) ?: return
        info { "revoking token id=${exists.id.value}" }
        try {
            runBlocking {
                apiService.revokeToken(exists.accessToken)
                apiService.revokeToken(exists.refreshToken)
            }
        } catch (e: Exception) {
            warn(e) { "revoke token failed" }
        }
        DiscordOAuthTokens.deleteWhere { DiscordOAuthTokens.member eq userId }
    }

    private fun getUserTokenEntity(userId: Long): DiscordOAuthTokenEntity? {
        return DiscordOAuthTokens.selectAll()
            .where { DiscordOAuthTokens.member eq userId }
            .limit(1)
            .singleOrNull()
            ?.let { DiscordOAuthTokenEntity.wrapRow(it) }
    }
}