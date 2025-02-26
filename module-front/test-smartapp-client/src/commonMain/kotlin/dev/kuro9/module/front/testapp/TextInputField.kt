package dev.kuro9.module.front.testapp

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*

@Composable
fun TextInputField() {
    var input by remember { mutableStateOf("") }

    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text("DeviceId") }
    )
}