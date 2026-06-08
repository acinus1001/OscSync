package dev.kuro9.module.front.application.homepage.page.services.iot

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.IotApiService
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.utils.requireAnyAuthority
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.smartthings.SmartAppUserDevice
import dev.kuro9.multiplatform.common.types.smartthings.event.SmartAppDeviceEvent
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject
import org.w3c.dom.EventSource
import org.w3c.dom.EventSourceInit

@Composable
fun IotRoot() = requireAnyAuthority("AUTHORITY_HOMEPAGE_IOT") { isLoading ->
    if (isLoading) return@requireAnyAuthority

    val serverInfo: ServerInfo = koinInject()
    val iotApiService: IotApiService = koinInject()
    val scope = rememberCoroutineScope()
    val routeState: RouteViewModel = koinInject()

    val devices = remember { mutableStateMapOf<String, SmartAppUserDevice>() }
    val deviceStates = remember { mutableStateMapOf<String, Boolean?>() }
    val isNetworkWaiting = remember { mutableStateOf(false) } // api 응답 오기 전까지 다른 버튼 비활성화 용도

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
            EventSourceInit(withCredentials = true)
        )

        eventSource.onmessage = { event ->
            val deviceEvent = minifyJson.decodeFromString<SmartAppDeviceEvent>(event.data.toString())
            println("device event: $deviceEvent")
            deviceStates[deviceEvent.deviceId] = deviceEvent.state
        }

        eventSource.onerror = { event ->
            println(event)
            println("sse error")
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
                                    true -> "ON"
                                    false -> "OFF"
                                    null -> "-"
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
                                        if (isNetworkWaiting.value) disabled()
                                        onClick {
                                            if (isNetworkWaiting.value) return@onClick
                                            scope.launch {
                                                handleApiException(routeState) {
                                                    isNetworkWaiting.value = true
                                                    iotApiService.executeRootDevices(device.deviceId, true)
                                                    isNetworkWaiting.value = false
                                                }
                                            }
                                        }
                                    }
                                )
                                Text("ON")
                            }

                            Label {
                                RadioInput(
                                    checked = currentState == false,
                                    attrs = {
                                        name(device.deviceId)
                                        if (isNetworkWaiting.value) disabled()
                                        onClick {
                                            if (isNetworkWaiting.value) return@onClick
                                            scope.launch {
                                                handleApiException(routeState) {
                                                    isNetworkWaiting.value = true
                                                    iotApiService.executeRootDevices(device.deviceId, false)
                                                    isNetworkWaiting.value = false
                                                }
                                            }
                                        }
                                    }
                                )
                                Text("OFF")
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun handleApiException(routeState: RouteViewModel, action: suspend () -> Unit) {
    try {
        action()
    } catch (e: ClientRequestException) {
        e.printStackTrace()

        when (e.response.status) {
            HttpStatusCode.Forbidden -> {
                window.alert("권한이 없습니다. 메인 페이지로 이동합니다.")
                routeState.navigate(Route.HOME)
                return
            }

            HttpStatusCode.Unauthorized -> {
                window.alert("인증되지 않은 사용자입니다. 메인 페이지로 이동합니다.")
                routeState.navigate(Route.HOME)
                return
            }
        }

        window.alert("알 수 없는 클라이언트 오류가 발생했습니다.")
    } catch (e: ServerResponseException) {
        e.printStackTrace()
        window.alert("알 수 없는 서버 오류가 발생하였습니다.")
    } catch (e: Exception) {
        e.printStackTrace()
        window.alert("알 수 없는 오류가 발생하였습니다.")
    }
}
