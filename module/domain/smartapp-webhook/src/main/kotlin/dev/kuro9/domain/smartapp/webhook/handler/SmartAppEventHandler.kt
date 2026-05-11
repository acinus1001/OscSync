package dev.kuro9.domain.smartapp.webhook.handler

import com.smartthings.sdk.smartapp.core.Response
import com.smartthings.sdk.smartapp.core.extensions.EventHandler
import com.smartthings.sdk.smartapp.core.models.EventResponseData
import com.smartthings.sdk.smartapp.core.models.EventType
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse
import dev.kuro9.multiplatform.common.types.smartthings.event.SmartAppDeviceEvent
import io.github.harryjhin.slf4j.extension.info
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class SmartAppEventHandler(
    private val eventPublisher: ApplicationEventPublisher
) : EventHandler {

    override fun handle(executionRequest: ExecutionRequest): ExecutionResponse {
        info { "EVENT: executionRequest = $executionRequest" }
        if (executionRequest.eventData == null)
            return Response.status(400)

        executionRequest.eventData.events.forEach { event ->
            when (event.eventType) {
                EventType.DEVICE_EVENT -> {
                    // 추후 switch 외의 capability 처리 시 아래 로직 수정 필요
                    if (event.deviceEvent.capability != "switch") return Response.ok(EventResponseData())

                    eventPublisher.publishEvent(
                        SmartAppDeviceEvent(
                            capability = event.deviceEvent.capability,
                            componentId = event.deviceEvent.componentId,
                            deviceId = event.deviceEvent.deviceId,
                            state = event.deviceEvent.value == "on"
                        )
                    )
                }

                EventType.MODE_EVENT -> {
                    info { event.modeEvent.toString() }
                }

                EventType.TIMER_EVENT -> {
                    info { event.timerEvent.toString() }
                }

                EventType.DEVICE_COMMANDS_EVENT -> {
                    info { event.deviceCommandsEvent.toString() }
                }

                null -> return Response.status(400)
            }
        }

        return Response.ok(EventResponseData())
    }
}