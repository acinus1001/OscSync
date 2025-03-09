package dev.kuro9.module.front.discord.app.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.kuro9.module.front.discord.app.component.user.UserInfoButtonComponent
import dev.kuro9.module.front.discord.app.component.user.database.UserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore
import dev.kuro9.module.front.discord.app.component.user.store.userInfoStore
import kotlin.coroutines.CoroutineContext

@Composable
@Suppress("FunctionName")
fun MainPage(
    storeFactory: StoreFactory,
    userInfoDatabase: UserInfoDatabase,
    mainContext: CoroutineContext,
    ioContext: CoroutineContext,
) {
    val userInfoStore = storeFactory.userInfoStore(
        database = userInfoDatabase,
        mainContext = mainContext,
        ioContext = ioContext,
    )
    val userInfoState: UserInfoStore.State by userInfoStore.states.collectAsState(
        UserInfoStore.State(userInfo = null),
        mainContext
    )

    UserInfoButtonComponent(userInfoState.toString()) {
        userInfoStore.accept(UserInfoStore.Intent.DeleteUserInfo)
    }
}