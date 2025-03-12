package dev.kuro9.module.front.discord.app.component.user

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
@Suppress("FunctionName")
fun UserInfoButtonView(
    userInfoText: String,
    isLoggedIn: Boolean,
    toLogInClick: () -> Unit,
    toLogoutClick: () -> Unit,
) {
    Column {
        Text(userInfoText)

        Button(onClick = if (isLoggedIn) toLogoutClick else toLogInClick) {
            Text(if (isLoggedIn) "Logout" else "Login")
        }
    }
}