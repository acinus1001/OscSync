package dev.kuro9.oscsync.osc

import dev.kuro9.oscsync.common.infoLog
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class OscLogger {

    @[Async EventListener]
    fun logOscEvent(event: VrcOscEvent<*>) {
        infoLog("event=$event")
    }
}