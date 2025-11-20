package dev.kuro9.application.batch.chess.job

import com.navercorp.spring.batch.plus.kotlin.configuration.BatchDsl
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemProcessor
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamReader
import com.navercorp.spring.batch.plus.kotlin.step.adapter.asItemStreamWriter
import dev.kuro9.application.batch.chess.tasklet.ChessComUserStatFetchTasklet
import dev.kuro9.application.batch.common.BatchErrorListener
import dev.kuro9.application.batch.common.handleFlow
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ChessComFetchJobConfig(
    private val batch: BatchDsl,
    private val txManager: PlatformTransactionManager,
    private val errorListener: BatchErrorListener,
    private val fetchTasklet: ChessComUserStatFetchTasklet,
) {

    @Bean
    fun chessComFetchJob(): Job = batch {
        job("chessComFetchJob") {
            step(chessComStatFetchStep()) {
                handleFlow { end() }
            }
            listener(errorListener)
        }
    }

    @Bean
    fun chessComStatFetchStep(): Step = batch {
        step("chessComStatFetchStep") {
            allowStartIfComplete(true)
            chunk(1, txManager) {
                reader(fetchTasklet.asItemStreamReader())
                processor(fetchTasklet.asItemProcessor())
                writer(fetchTasklet.asItemStreamWriter())

                listener(errorListener)
            }
        }
    }
}