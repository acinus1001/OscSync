@file:OptIn(ExperimentalTime::class)

package dev.kuro9.domain.chess.service

import dev.kuro9.domain.chess.dto.ChessComUserStat
import dev.kuro9.domain.chess.enums.EloType
import dev.kuro9.domain.chess.exception.ChessComUserNotRegisteredException
import dev.kuro9.internal.chess.api.dto.ChessComUser
import dev.kuro9.internal.chess.api.service.ChessComApiService
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import kotlin.time.ExperimentalTime

@Service
class ChessComUserProfileService(
    private val apiService: ChessComApiService,
    private val userService: ChessComUserService,
) {

    // registered
    @Throws(ChessComUserNotRegisteredException::class)
    @Cacheable(value = ["cache-1m"], key = "'ChessComUserProfileService#getUserProfile(userId)' + #userId")
    suspend fun getUserProfile(userId: Long): ChessComUserStat.Registered {
        val username = userService.getUser(userId)?.username ?: throw ChessComUserNotRegisteredException()
        val infoWithoutDb = getUserProfile(username)

        val timelines = EloType.entries.associateWith { eloType ->
            userService.getEloTimeline(
                userId = userId,
                eloType = eloType,
                dateRange = LocalDate.now().let { it.minus(7, DateTimeUnit.DAY)..it }
            )
        }

        return infoWithoutDb.asRegistered(timelines)
    }

    // guest user
    @Cacheable(value = ["cache-1m"], key = "'ChessComUserProfileService#getUserProfile(username)' + #username")
    suspend fun getUserProfile(username: String): ChessComUserStat.Guest {
        val user: ChessComUser = apiService.getUser(username)
        val userStat = apiService.getUserStat(username)

        val eloMap = buildMap {
            userStat.chessDaily?.last?.let { stat ->
                put(EloType.DAILY, stat.rating)
            }
            userStat.chess960Daily?.last?.let { stat ->
                put(EloType.DAILY960, stat.rating)
            }
            userStat.chessRapid?.last?.let { stat ->
                put(EloType.RAPID, stat.rating)
            }
            userStat.chessBullet?.last?.let { stat ->
                put(EloType.BULLET, stat.rating)
            }
            userStat.chessBlitz?.last?.let { stat ->
                put(EloType.BLITZ, stat.rating)
            }
        }

        return ChessComUserStat.Guest(
            username = user.username,
            avatarUrl = user.avatar,
            joinedAt = user.joined,
            lastOnline = user.lastOnline,
            profileUrl = user.url,
            eloMap = eloMap,
        )
    }
}