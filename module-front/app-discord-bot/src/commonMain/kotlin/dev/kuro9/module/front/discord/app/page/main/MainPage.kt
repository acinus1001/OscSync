package dev.kuro9.module.front.discord.app.page.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.kuro9.module.front.discord.app.component.user.UserInfoButtonView
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore

@Composable
@Suppress("FunctionName")
fun MainPage(
    component: MainScreenComponent,
) {
    val userInfoState: UserInfoStore.State by component.userInfoStates.collectAsState()
    Column {
        UserInfoButtonView(
            userInfoText = userInfoState.toString(),
            onLogoutClick = {
                component.userInfoStore.accept(UserInfoStore.Intent.DeleteUserInfo)
            }
        )

    }
}