package dev.kuro9.application.batch.f1.news.scheduler

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class F1NewsParseJobScheduler(
    private val jobOperator: JobOperator,
    private val f1NewsParseJob: Job
) {

    @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
    fun runF1NewsParseJob() {
        val currentTime = LocalDateTime.now().time
        val hourIndex = (currentTime.hour + 1).toLong()

        JobParametersBuilder()
            .addLocalDate("executeDate", java.time.LocalDate.now())
            .addLong("hourIndex", hourIndex)
            .toJobParameters()
            .let { jobOperator.start(f1NewsParseJob, it) }
    }
}