package dev.kuro9.module.front.application.homepage.page.services.iot

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.iot.IotApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.smartthings.SmartAppUserDevice
import dev.kuro9.multiplatform.common.types.smartthings.event.SmartAppDeviceEvent
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
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
    val scope = rememberCoroutineScope()

    val devices = remember { mutableStateMapOf<String, SmartAppUserDevice>() }
    val deviceStates = remember { mutableStateMapOf<String, Boolean?>() }

    LaunchedEffect(Unit) {
        val deviceList = iotApiService.getRootIotDevices()
        deviceList.forEach {
            devices[it.deviceId] = it
            // 초기 상태는 모름(null)으로 설정
            if (it.deviceId !in deviceStates) {
                deviceStates[it.deviceId] = null
            }
        }
    }

    DisposableEffect(Unit) {
        val eventSource = EventSource(
            "${serverInfo.protocol.name}://${serverInfo.host}:${serverInfo.port}/services/iot/noti/subscribe",
            eventSourceInit(withCredentials = true)
        )

        eventSource.onmessage = { event ->
            println(event.data.toString())
            val deviceEvent = minifyJson.decodeFromString<SmartAppDeviceEvent>(event.data.toString())
            deviceStates[deviceEvent.deviceId] = deviceEvent.state
        }

        eventSource.onerror = { event ->
            println(event)

            // todo 서버 끊겼다는 인디케이터 표시
        }

        onDispose {
            eventSource.close()
        }
    }

    Div(attrs = {
        style {
            padding(20.px)
            color(Color("#f1f1f1"))
        }
    }) {
        H2 { Text("IoT 제어") }

        Table(attrs = {
            style {
                width(100.percent)
                property("border-collapse", "collapse")
            }
        }) {
            Thead {
                Tr {
                    Th { Text("기기명") }
                    Th { Text("현재 상태") }
                    Th { Text("제어") }
                }
            }
            Tbody {
                devices.values.forEach { device ->
                    val currentState = deviceStates[device.deviceId]
                    Tr {
                        Td(attrs = { style { padding(8.px); textAlign("center") } }) {
                            Text(device.deviceName)
                        }
                        Td(attrs = { style { padding(8.px); textAlign("center") } }) {
                            Text(
                                when (currentState) {
                                    true -> "켜짐"
                                    false -> "꺼짐"
                                    null -> "모름"
                                }
                            )
                        }
                        Td(attrs = {
                            style {
                                padding(8.px)
                                display(DisplayStyle.Flex)
                                justifyContent(JustifyContent.Center)
                                gap(10.px)
                            }
                        }) {
                            Label {
                                RadioInput(
                                    checked = currentState == true,
                                    attrs = {
                                        name(device.deviceId)
                                        onClick {
                                            scope.launch {
                                                iotApiService.executeRootDevices(device.deviceId, true)
                                            }
                                        }
                                    }
                                )
                                Text("켜기")
                            }

                            Label {
                                RadioInput(
                                    checked = currentState == false,
                                    attrs = {
                                        name(device.deviceId)
                                        onClick {
                                            scope.launch {
                                                iotApiService.executeRootDevices(device.deviceId, false)
                                            }
                                        }
                                    }
                                )
                                Text("끄기")
                            }
                        }
                    }
                }
            }
        }
    }
}