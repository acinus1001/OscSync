package dev.kuro9.application.batch.karaoke.scheduler

import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import kotlinx.datetime.toJavaLocalDate
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KaraokeCrawlJobScheduler(
    private val jobOperator: JobOperator,
    private val karaokeCrawlJob: Job
) {

    @Scheduled(cron = "10 10 9,10,12,18 * * *", zone = "Asia/Seoul")
    fun runKaraokeJob() {
        JobParametersBuilder()
            .addLocalDate("executeDate", LocalDate.now().toJavaLocalDate())
            .addString(
                "executeTimeKey", "${
                    LocalDate.now().let {
                        "${it.year}${it.month.number.toString().padStart(2, '0')}${
                            it.day.toString().padStart(2, '0')
                        }"
                    }
                }-${LocalTime.now().hour}"
            )
            .toJobParameters()
            .let { jobOperator.start(karaokeCrawlJob, it) }
    }
}