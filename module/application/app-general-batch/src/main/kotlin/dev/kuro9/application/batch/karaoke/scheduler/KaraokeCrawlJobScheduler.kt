package dev.kuro9.application.batch.karaoke.scheduler

import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class KaraokeCrawlJobScheduler(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
) {

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    fun runKaraokeJob() {
        val job = jobRegistry.getJob("karaokeCrawlJob")
        jobLauncher.run(
            job,
            JobParametersBuilder()
                .addLocalDate("executeDate", LocalDate.now())
                .toJobParameters()
        )
    }
}