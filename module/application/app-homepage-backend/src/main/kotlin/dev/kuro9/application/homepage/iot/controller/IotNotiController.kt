package dev.kuro9.application.homepage.iot.controller

import dev.kuro9.application.homepage.iot.service.IotNotiService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping("/services/iot/noti")
class IotNotiController(
    private val notiService: IotNotiService
) {

    @GetMapping(value = ["/subscribe"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(60L * 1000 * 60) // 1시간

        emitter.onCompletion { notiService.removeEmitter(emitter) }
        emitter.onTimeout { notiService.removeEmitter(emitter) }
        emitter.onError { e -> notiService.removeEmitter(emitter) }

        notiService.addEmitter(emitter)

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"))
        } catch (e: Exception) {
            emitter.completeWithError(e)
        }

        return emitter
    }
}