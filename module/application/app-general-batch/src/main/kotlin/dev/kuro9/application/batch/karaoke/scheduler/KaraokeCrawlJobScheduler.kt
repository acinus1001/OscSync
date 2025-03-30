package dev.kuro9.application.batch.karaoke.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KaraokeCrawlJobScheduler {

    @Scheduled(cron = "0 0 9 * * *")
    fun runKaraokeJob() {
        TODO()
    }
}