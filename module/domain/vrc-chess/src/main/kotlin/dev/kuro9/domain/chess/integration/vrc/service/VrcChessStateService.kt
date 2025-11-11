package dev.kuro9.domain.chess.integration.vrc.service

import dev.kuro9.domain.chess.integration.vrc.dto.ChessPlayerInfo
import dev.kuro9.domain.chess.integration.vrc.enums.ChessTurn
import dev.kuro9.domain.chess.integration.vrc.repository.table.VrcChessGameEntity
import dev.kuro9.domain.chess.integration.vrc.repository.table.VrcChessGameLogEntity
import dev.kuro9.domain.chess.integration.vrc.repository.table.VrcChessGameLogs
import dev.kuro9.domain.chess.integration.vrc.repository.table.VrcChessGames
import dev.kuro9.domain.database.exists
import dev.kuro9.domain.database.fetchFirstOrNull
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VrcChessStateService {

    fun isPlaying(userIpHash: Int): Boolean {

        return VrcChessGames.select(VrcChessGames.id)
            .where(getUserPlayingGameOp(userIpHash))
            .exists()
    }

    fun getPlaying(user: ChessPlayerInfo.User): VrcChessGameEntity? {

        return VrcChessGameEntity.find { getUserPlayingGameOp(user.ipHash) }
            .limit(1)
            .firstOrNull()
    }

    fun getPlayingFen(userIpHash: Int): String? {

        return VrcChessGames.select(VrcChessGames.fen)
            .where(getUserPlayingGameOp(userIpHash))
            .fetchFirstOrNull(VrcChessGames.fen)
    }

    fun getGame(white: ChessPlayerInfo, black: ChessPlayerInfo): VrcChessGameEntity? {
        return VrcChessGameEntity.find {
            (VrcChessGames.whiteUserIpHash eq white.ipHash)
                .and(VrcChessGames.blackUserIpHash eq black.ipHash)
                .and(VrcChessGames.endedAt.isNull())
        }
            .limit(2)
            .firstOrNull()
    }

    fun getAllGameLog(gameId: Long): List<VrcChessGameLogEntity> {
        return VrcChessGameLogEntity.find {
            (VrcChessGameLogs.gameId eq gameId)
                .and(VrcChessGameLogs.undoAt.isNull())
        }
            .orderBy(VrcChessGameLogs.id to SortOrder.ASC)
            .toList()
    }

    fun getBotGame(user: ChessPlayerInfo.User): VrcChessGameEntity? {
        return VrcChessGameEntity.find { getUserPlayingGameOp(user.ipHash) }
            .limit(1)
            .firstOrNull()
    }

    @Transactional(rollbackFor = [Exception::class])
    fun endAllGame(user: ChessPlayerInfo.User) {
        VrcChessGames.update(where = { getUserPlayingGameOp(user.ipHash) }) {
            it[VrcChessGames.endedAt] = LocalDateTime.now()
        }
    }

    /**
     * @param botELO 봇 elo 세팅시 입력. 봇 없으면 입력하지 않아도 됨
     */
    @Transactional(rollbackFor = [Exception::class])
    fun startNewGame(white: ChessPlayerInfo, black: ChessPlayerInfo, botELO: Int? = null): VrcChessGameEntity {

        // 진행중인 게임 모두 종료 ( 봇 제외 )
        listOf(white, black)
            .filterIsInstance<ChessPlayerInfo.User>()
            .forEach { user ->
                VrcChessGames.update(where = { getUserPlayingGameOp(user.ipHash) }) {
                    it[VrcChessGames.endedAt] = LocalDateTime.now()
                }
            }


        // 새 게임 Insert
        return VrcChessGameEntity.new {

            this.whiteUserIpHash = white.ipHash
            this.blackUserIpHash = black.ipHash
            this.botELO = botELO

            this.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" // 초기 fen

            this.createdAt = LocalDateTime.now()
            this.fenUpdatedAt = LocalDateTime.now()
            this.endedAt = null
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateMove(
        white: ChessPlayerInfo,
        black: ChessPlayerInfo,
        nowTurn: ChessTurn,
        newFen: String,
        move: String,
        pgn: String
    ) {
        val gameEntity = getGame(white = white, black = black) ?: throw IllegalStateException("Game not found")
        val lastMove = VrcChessGameLogEntity.find {
            (VrcChessGameLogs.gameId eq gameEntity.id.value)
                .and(VrcChessGameLogs.undoAt.isNull())
        }
            .limit(1)
            .firstOrNull()

        check(nowTurn == ChessTurn.WHITE || lastMove != null) { "잘못된 게임 순서" }

        val nowTurnUser = when (nowTurn) {
            ChessTurn.WHITE -> white
            ChessTurn.BLACK -> black
        }

        VrcChessGameLogs.insert {
            it[this.userIpHash] = nowTurnUser.ipHash
            it[this.gameId] = gameEntity.id.value
            it[this.fen] = newFen
            it[this.move] = move
            it[this.pgn] = pgn

            it[this.turnType] = nowTurn
            it[this.isBotMove] = nowTurnUser is ChessPlayerInfo.Bot

            it[this.createdAt] = LocalDateTime.now()
            it[this.undoAt] = null
        }
        gameEntity.fen = newFen
        gameEntity.fenUpdatedAt = LocalDateTime.now()
    }

    private fun getUserPlayingGameOp(userIpHash: Int): Op<Boolean> {
        return ((VrcChessGames.whiteUserIpHash eq userIpHash) or (VrcChessGames.blackUserIpHash eq userIpHash))
            .and(VrcChessGames.endedAt.isNull())
    }
}