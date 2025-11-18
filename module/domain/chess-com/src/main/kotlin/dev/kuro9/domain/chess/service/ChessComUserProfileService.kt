package dev.kuro9.domain.chess.service

import dev.kuro9.domain.chess.exception.ChessComUserNotRegisteredException
import dev.kuro9.internal.chess.api.service.ChessComApiService
import org.springframework.stereotype.Service

@Service
class ChessComUserProfileService(
    private val apiService: ChessComApiService,
    private val userService: ChessComUserService,
) {

    @Throws(ChessComUserNotRegisteredException::class)
    fun getUserProfile(userId: Long) {
        val username = userService.getUser(userId)?.username ?: throw ChessComUserNotRegisteredException()
        return getUserProfile(username)
    }

    fun getUserProfile(username: String) {
        // TODO
    }
}