package dev.kuro9.application.batch.f1.news.scheduler

import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class F1NewsParseJobScheduler(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
) {

    @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
    fun runF1NewsParseJob() {
        val currentTime = java.time.LocalTime.now()
        val hourIndex = (currentTime.hour + 1).toLong()

        val job = jobRegistry.getJob("f1NewsParseJob")
        jobLauncher.run(
            job,
            JobParametersBuilder()
                .addLocalDate("executeDate", java.time.LocalDate.now())
                .addLong("hourIndex", hourIndex)
                .toJobParameters()
        )
    }
}