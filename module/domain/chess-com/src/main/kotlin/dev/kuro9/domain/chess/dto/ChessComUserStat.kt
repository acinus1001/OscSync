package dev.kuro9.domain.chess.dto

import dev.kuro9.domain.chess.enums.EloType
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed interface ChessComUserStat : java.io.Serializable {
    val username: String
    val avatarUrl: String?
    val joinedAt: Instant
    val lastOnline: Instant
    val profileUrl: String
    val eloMap: EloMap
    val eloChangeTimeline: Map<EloType, List<Pair<LocalDate, Int>>>?

    data class Registered(
        override val username: String,
        override val avatarUrl: String?,
        override val joinedAt: Instant,
        override val lastOnline: Instant,
        override val profileUrl: String,
        override val eloMap: EloMap,
        override val eloChangeTimeline: Map<EloType, List<Pair<LocalDate, Int>>>,
    ) : ChessComUserStat, java.io.Serializable

    data class Guest(
        override val username: String,
        override val avatarUrl: String?,
        override val joinedAt: Instant,
        override val lastOnline: Instant,
        override val profileUrl: String,
        override val eloMap: EloMap,
    ) : ChessComUserStat, java.io.Serializable {
        override val eloChangeTimeline = null

        fun asRegistered(eloChangeTimeline: Map<EloType, List<Pair<LocalDate, Int>>>) = Registered(
            username = username,
            avatarUrl = avatarUrl,
            joinedAt = joinedAt,
            lastOnline = lastOnline,
            profileUrl = profileUrl,
            eloMap = eloMap,
            eloChangeTimeline = eloChangeTimeline
        )
    }
}

private typealias EloMap = Map<EloType, Int>