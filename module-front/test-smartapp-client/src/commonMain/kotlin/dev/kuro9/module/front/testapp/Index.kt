package dev.kuro9.module.front.testapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Index(
    deviceId: String,
    onDeviceIdChange: (String) -> Unit,
    deviceState: Boolean,
    onDeviceStateChange: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = deviceId,
            onValueChange = onDeviceIdChange,
            label = { Text("DeviceId") }
        )

        Switch(
            checked = deviceState,
            onCheckedChange = onDeviceStateChange,
        )
    }
}