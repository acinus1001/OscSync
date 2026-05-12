package dev.kuro9.domain.member.auth.service

import dev.kuro9.domain.member.auth.repository.RefreshTokenEntity
import dev.kuro9.domain.member.auth.repository.RefreshTokens
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RefreshTokenService {

    fun saveToken(userId: Long, token: String, expiresAt: LocalDateTime, createdAt: LocalDateTime) {
        RefreshTokens.insert {
            it[RefreshTokens.member] = userId
            it[RefreshTokens.token] = token
            it[RefreshTokens.expiresAt] = expiresAt
            it[RefreshTokens.createdAt] = createdAt
        }
    }

    @Transactional(readOnly = true)
    fun findByToken(token: String): RefreshTokenInfo? {
        return RefreshTokenEntity.find { RefreshTokens.token eq token }
            .singleOrNull()
            ?.let {
                RefreshTokenInfo(
                    userId = it.member.id.value,
                    token = it.token,
                    expiresAt = it.expiresAt
                )
            }
    }

    fun deleteByUserId(userId: Long) {
        RefreshTokens.deleteWhere { RefreshTokens.member eq userId }
    }

    fun deleteByToken(token: String) {
        RefreshTokens.deleteWhere { RefreshTokens.token eq token }
    }
}

data class RefreshTokenInfo(
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime
)
