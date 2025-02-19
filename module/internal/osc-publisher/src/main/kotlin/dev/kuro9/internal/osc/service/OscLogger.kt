package dev.kuro9.internal.osc.service

import dev.kuro9.common.util.infoLog
import dev.kuro9.internal.osc.model.VrcOscReceiveEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class OscLogger {

    @[Async EventListener]
    fun logOscEvent(event: VrcOscReceiveEvent<*>) {
        infoLog("event=$event")
    }
}