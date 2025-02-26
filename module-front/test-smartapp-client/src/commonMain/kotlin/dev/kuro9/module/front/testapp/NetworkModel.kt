package dev.kuro9.module.front.testapp

import androidx.compose.runtime.*
import dev.kuro9.multiplatform.common.network.httpClient
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.kuro9.multiplatform.common.types.testapp.request.SmartAppSwitchControlRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch

@Composable
fun NetworkModel() {
    val client = remember {
        httpClient {
            install(ContentNegotiation) {
                json(minifyJson)
            }
        }
    }
    val scope = rememberCoroutineScope()
    var deviceId by remember { mutableStateOf("") }
    var deviceState by remember { mutableStateOf(false) }

    Index(
        deviceId = deviceId,
        onDeviceIdChange = { deviceId = it },
        deviceState = deviceState,
        onDeviceStateChange = {
            deviceState = it

            scope.launch {
                client.post("http://localhost:8080/test/switch") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        SmartAppSwitchControlRequest(
                            deviceId = deviceId,
                            value = it
                        )
                    )
                }
            }

        },
    )
}