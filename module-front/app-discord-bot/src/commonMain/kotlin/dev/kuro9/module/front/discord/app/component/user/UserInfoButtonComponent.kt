package dev.kuro9.module.front.discord.app.component.user

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
@Suppress("FunctionName")
fun UserInfoButtonComponent(
    userInfoText: String,
    onLogoutClick: () -> Unit,
) {
    Column {
        Text(userInfoText)

        Button(onClick = onLogoutClick) {
            Text("SAMPLE USAGE")
        }
    }
}