package dev.kuro9.module.front.application.homepage.page.services.iot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import dev.kuro9.module.front.application.homepage.network.iot.IotApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.smartthings.event.SmartAppDeviceEvent
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.EventSource
import org.w3c.dom.EventSourceInit

private fun eventSourceInit(
    withCredentials: Boolean,
): EventSourceInit =
    js("{}").unsafeCast<EventSourceInit>().also {
        it.withCredentials = withCredentials
    }

@Composable
fun IotRoot() {
    val serverInfo: ServerInfo = koinInject()
    val iotApiService: IotApiService = koinInject()

    DisposableEffect(Unit) {
        val eventSource = EventSource(
            "${serverInfo.protocol}://${serverInfo.host}:${serverInfo.port}/services/iot/noti/subscribe",
            eventSourceInit(withCredentials = true)
        )

        eventSource.onmessage = { event ->
            val deviceEvent = minifyJson.decodeFromString<SmartAppDeviceEvent>(event.data.toString())

            // todo 기기 상태 업데이트
        }

        eventSource.onerror = { event ->
            println(event)

            // todo 서버 끊겼다는 인디케이터 표시
        }

        onDispose {
            eventSource.close()
        }
    }

    H2 { Text("IoT 알림") }


}