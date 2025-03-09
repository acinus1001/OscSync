package dev.kuro9.module.front.discord.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.kuro9.module.front.discord.app.component.user.database.NetworkUserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.store.userInfoStore
import dev.kuro9.module.front.discord.app.global.GlobalStores
import dev.kuro9.module.front.discord.app.page.RootComponent
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val globalStores = GlobalStores(
        userInfoStore = DefaultStoreFactory().userInfoStore(
            database = NetworkUserInfoDatabase(),
            mainContext = Dispatchers.Main.immediate,
            ioContext = Dispatchers.Main
        )
    )
    val root = RootComponent(DefaultComponentContext(lifecycle), globalStores)
    ComposeViewport(document.body!!) {
        App(root)
    }
}