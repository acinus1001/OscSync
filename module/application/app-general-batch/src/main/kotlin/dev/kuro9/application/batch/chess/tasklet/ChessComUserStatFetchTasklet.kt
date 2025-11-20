package dev.kuro9.application.batch.chess.tasklet

import com.navercorp.spring.batch.plus.step.adapter.ItemStreamIterableReaderProcessorWriter
import dev.kuro9.domain.chess.enums.EloType
import dev.kuro9.domain.chess.repository.table.ChessComUsers
import dev.kuro9.domain.chess.service.ChessComUserService
import dev.kuro9.internal.chess.api.service.ChessComApiService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@[StepScope Component Transactional]
class ChessComUserStatFetchTasklet(
    private val chessApiService: ChessComApiService,
    private val chessComUserService: ChessComUserService,
) : ItemStreamIterableReaderProcessorWriter<Pair<Long, String>, Pair<Long, Map<EloType, Int>>> {

    // 업데이트 필요한 유저 가져오기
    override fun readIterable(executionContext: ExecutionContext): Iterable<Pair<Long, String>> {
        return ChessComUsers
            .select(ChessComUsers.userId, ChessComUsers.username)
            .map { it[ChessComUsers.userId].value to it[ChessComUsers.username] }
    }

    // 유저 정보 api 통해 가져오기
    override fun process(item: Pair<Long, String>): Pair<Long, Map<EloType, Int>> {
        val (userId, username) = item
        val userStat = runBlocking { chessApiService.getUserStat(username) }

        return userId to buildMap {
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
    }

    // 유저 정보 업데이트
    override fun write(chunk: Chunk<out Pair<Long, Map<EloType, Int>>>) {

        for ((userId, eloMap) in chunk) {
            for ((eloType, elo) in eloMap) {
                chessComUserService.insertElo(
                    userId = userId,
                    eloType = eloType,
                    elo = elo,
                )
            }
        }
    }
}