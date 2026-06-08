package dev.kuro9.domain.member.auth.service

import dev.kuro9.domain.member.auth.network.DiscordOAuthApiService
import dev.kuro9.domain.member.auth.repository.DiscordOAuthTokenEntity
import dev.kuro9.domain.member.auth.repository.DiscordOAuthTokens
import dev.kuro9.multiplatform.common.date.util.now
import io.github.harryjhin.slf4j.extension.warn
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DiscordOAuth2TokenManageService(
    private val apiService: DiscordOAuthApiService,
) {
    suspend fun getToken(userId: Long): DiscordOAuthTokenEntity? = suspendTransaction {
        val token = DiscordOAuthTokenEntity.find { DiscordOAuthTokens.member eq userId }.singleOrNull()
            ?: return@suspendTransaction null
        if (token.expiresAt <= LocalDateTime.now()) {
            return@suspendTransaction refreshToken(userId)
        }

        return@suspendTransaction token
    }

    suspend fun saveToken(
        userId: Long,
        accessToken: String,
        refreshToken: String,
        expiresAt: LocalDateTime,
    ): Unit = suspendTransaction {
        val exists = DiscordOAuthTokenEntity.find { DiscordOAuthTokens.member eq userId }.singleOrNull()
        if (exists != null) {
            // revoke token
            revokeToken(userId)
        }

        DiscordOAuthTokens.insert {
            it[this.member] = userId
            it[this.accessToken] = accessToken
            it[this.refreshToken] = refreshToken
            it[this.expiresAt] = expiresAt
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    suspend fun refreshToken(
        userId: Long,
    ): DiscordOAuthTokenEntity? = suspendTransaction {
        val exists = DiscordOAuthTokenEntity.find { DiscordOAuthTokens.member eq userId }.singleOrNull()
            ?: return@suspendTransaction null

        val response = try {
            apiService.refreshToken(exists.refreshToken)
        } catch (e: Exception) {
            warn(e) { "refresh token failed" }
            revokeToken(userId)
            exists.delete()
            return@suspendTransaction null
        }

        exists.accessToken = response.accessToken
        exists.refreshToken = response.refreshToken
        exists.expiresAt = response.expiresAt
        exists.updatedAt = LocalDateTime.now()

        return@suspendTransaction exists
    }

    suspend fun revokeToken(userId: Long): Unit = suspendTransaction {
        val exists = DiscordOAuthTokenEntity.find { DiscordOAuthTokens.member eq userId }.singleOrNull()
            ?: return@suspendTransaction
        try {
            apiService.revokeToken(exists.accessToken)
            apiService.revokeToken(exists.refreshToken)
        } catch (e: Exception) {
            warn(e) { "revoke token failed" }
        }
        exists.delete()
    }
}