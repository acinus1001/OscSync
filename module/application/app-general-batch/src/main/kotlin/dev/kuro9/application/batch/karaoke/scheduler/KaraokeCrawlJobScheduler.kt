package dev.kuro9.application.batch.karaoke.scheduler

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KaraokeCrawlJobScheduler(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
) {

    @Scheduled(cron = "0 40 9 * * *", zone = "Asia/Seoul")
    fun runKaraokeJob() {
        val job = jobRegistry.getJob("karaokeCrawlJob")
        jobLauncher.run(
            job,
            JobParametersBuilder()
                .addLocalDate("executeDate", LocalDate.now().toJavaLocalDate())
                .addString("timeType", if (LocalTime.now().hour in 0..11) "AM" else "PM")
                .toJobParameters()
        )
    }
}