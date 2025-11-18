package dev.kuro9.domain.chess.service

import dev.kuro9.domain.chess.dto.ChessComUserRank
import dev.kuro9.domain.chess.enums.EloType
import dev.kuro9.domain.chess.repository.table.ChessComEloHistories
import dev.kuro9.domain.chess.repository.table.ChessComUserEntity
import dev.kuro9.domain.chess.repository.table.ChessComUsers
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChessComUserService {

    @Transactional
    fun upsertUser(
        userId: Long,
        guildId: Long,
        chessUserName: String,
        chessUserUrl: String,
        chessProfilePic: String?,
    ) {
        ChessComUsers.upsert(
            onUpdateExclude = listOf(ChessComUsers.createdAt),
        ) {
            it[this.userId] = userId
            it[this.guildId] = guildId
            it[this.username] = chessUserName
            it[this.userProfileUrl] = chessUserUrl
            it[this.profilePic] = chessProfilePic
            it[this.createdAt] = LocalDateTime.now()
            it[this.updatedAt] = LocalDateTime.now()
        }
    }

    @Transactional
    fun insertElo(
        userId: Long,
        eloType: EloType,
        elo: Int,
    ) {
        ChessComEloHistories.insert {
            it[this.userId] = userId
            it[this.eloType] = eloType
            it[this.elo] = elo
        }
    }

    fun getUser(userId: Long): ChessComUserEntity? {
        return ChessComUserEntity.findById(userId)
    }

    @Cacheable(value = ["cache-5m"], key = "'ChessComUserService#getRank' + #guildId + #eloType")
    fun getRank(
        guildId: Long,
        eloType: EloType,
    ): ChessComUserRank {
        val latestHistory = ChessComEloHistories
            .select(
                ChessComEloHistories.userId,
                ChessComEloHistories.elo
            )
            .where { ChessComEloHistories.eloType eq eloType }
            .withDistinctOn(ChessComEloHistories.userId to SortOrder.DESC)
            .orderBy(
                ChessComEloHistories.userId to SortOrder.ASC,
                ChessComEloHistories.createdAt to SortOrder.DESC
            )
            .alias("t")

        val rankList = ChessComUsers
            .join(
                otherTable = latestHistory,
                joinType = JoinType.LEFT,
                onColumn = ChessComUsers.userId,
                otherColumn = latestHistory[ChessComEloHistories.userId]
            )
            .selectAll()
            .where { ChessComUsers.guildId eq guildId }
            .orderBy(ChessComEloHistories.elo to SortOrder.DESC)
            .mapIndexed { index, row ->
                ChessComUserRank.UserInfo(
                    userId = row[ChessComUsers.userId].value,
                    chessComUserName = row[ChessComUsers.username],
                    chessUserUrl = row[ChessComUsers.userProfileUrl],
                    guildRank = index + 1,
                    elo = row.getOrNull(ChessComEloHistories.elo) ?: 0,
                )
            }

        return ChessComUserRank(
            guildId = guildId,
            eloType = eloType,
            rankList = rankList,
        )
    }
}