package dev.kuro9.module.front.discord.app.page.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.subscribe
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore
import dev.kuro9.module.front.discord.app.coroutines.UnconfinedCoroutineContext
import dev.kuro9.module.front.discord.app.util.goExternalPage
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainScreenComponent(
    componentContext: ComponentContext,
    val userInfoStore: UserInfoStore, // 전역으로 관리되는 store(유저 정보 -> 생성자 주입) // 특정 페이지에서만 관리한다면 컴포넌트에서 생성하기,
) : ComponentContext by componentContext {

    @OptIn(ExperimentalCoroutinesApi::class)
    val userInfoStates = userInfoStore.stateFlow

    init {
        componentContext.lifecycle.subscribe(onResume = {
            componentContext
            userInfoStore.labels.collectInScope { label ->
                KtorSimpleLogger("test").info("label: $label")
                when (label) {
                    is UserInfoStore.Label.Redirect -> goExternalPage(label.url)
                }
            }
        })
    }

    private fun <T> Flow<T>.collectInScope(onLabelReceived: (T) -> Unit) {
        CoroutineScope(UnconfinedCoroutineContext).launch {
            collect { onLabelReceived(it) }
        }
    }
}