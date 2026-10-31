package dev.kuro9.application.batch.chess.scheduler

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ChessComFetchJobScheduler(
    private val jobOperator: JobOperator,
    private val chessComFetchJob: Job,
) {

    //    @Scheduled(cron = "0 10 * * * *", zone = "Asia/Seoul")
    fun runChessComFetchJob() {
        val currentTime = LocalDateTime.now().time
        val hourIndex = (currentTime.hour + 1).toLong()

        JobParametersBuilder()
            .addLocalDate("executeDate", LocalDate.now())
            .addLong("hourIndex", hourIndex)
            .toJobParameters()
            .let { jobOperator.start(chessComFetchJob, it) }
    }
}