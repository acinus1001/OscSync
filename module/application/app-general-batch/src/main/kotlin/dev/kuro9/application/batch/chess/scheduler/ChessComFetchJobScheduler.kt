package dev.kuro9.application.batch.chess.scheduler

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ChessComFetchJobScheduler(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
) {

    @Scheduled(cron = "0 10 * * * *", zone = "Asia/Seoul")
    fun runChessComFetchJob() {
        val job = jobRegistry.getJob("chessComFetchJob")
        val currentTime = LocalDateTime.now().time
        val hourIndex = (currentTime.hour + 1).toLong()

        jobLauncher.run(
            job,
            JobParametersBuilder()
                .addLocalDate("executeDate", LocalDate.now())
                .addLong("hourIndex", hourIndex)
                .toJobParameters()
        )
    }
}