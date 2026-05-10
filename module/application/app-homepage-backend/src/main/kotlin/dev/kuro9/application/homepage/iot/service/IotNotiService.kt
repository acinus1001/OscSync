package dev.kuro9.application.homepage.iot.service

import dev.kuro9.common.logger.infoLog
import dev.kuro9.multiplatform.common.types.smartthings.event.SmartAppDeviceEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList


@Service
class IotNotiService {
    private val emitters: MutableList<SseEmitter> = CopyOnWriteArrayList()

    fun addEmitter(emitter: SseEmitter) {
        emitters.add(emitter)
    }

    fun removeEmitter(emitter: SseEmitter) {
        emitters.remove(emitter)
    }

    fun send(message: String, customEventName: String? = null) {
        emitters
            .filter { emitter ->
                val result = runCatching {
                    SseEmitter.event()
                        .data(message)
                        .let { if (customEventName != null) it.name(customEventName) else it }
                        .let { emitter.send(it) }
                }

                result.isFailure
            }
            .let { emitters.removeAll(it) }
    }

    @Scheduled(fixedDelay = 30_000L)
    fun sendHeartbeat() {
        send("ping", customEventName = "heartbeat")
    }

    @EventListener(SmartAppDeviceEvent::class)
    fun onSmartAppDeviceEvent(event: SmartAppDeviceEvent) {
        infoLog("smartapp device event: $event")
        send(event.toString())
    }
}