package dev.kuro9.module.front.discord.app.page.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainScreenComponent(
    componentContext: ComponentContext,
    val userInfoStore: UserInfoStore, // 전역으로 관리되는 store(유저 정보 -> 생성자 주입) // 특정 페이지에서만 관리한다면 컴포넌트에서 생성하기
) : ComponentContext by componentContext {

    @OptIn(ExperimentalCoroutinesApi::class)
    val userInfoStates = userInfoStore.stateFlow
}