package dev.kuro9.module.front.discord.app.page

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import dev.kuro9.module.front.discord.app.config.FragmentConfig
import dev.kuro9.module.front.discord.app.global.GlobalStores
import dev.kuro9.module.front.discord.app.page.main.MainScreenComponent

class RootComponent(
    componentContext: ComponentContext,
    private val globalStores: GlobalStores,
) : ComponentContext by componentContext {
    private val navigation = StackNavigation<FragmentConfig>()
    
    val childStack = childStack(
        source = navigation,
        serializer = FragmentConfig.serializer(),
        initialConfiguration = FragmentConfig.Main,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        config: FragmentConfig,
        context: ComponentContext,
    ): ChildComponent = when (config) {
        FragmentConfig.Main -> ChildComponent.Main(
            MainScreenComponent(
                componentContext = context,
                userInfoStore = globalStores.userInfoStore,
            )
        )
    }

    sealed class ChildComponent {
        data class Main(val component: MainScreenComponent) : ChildComponent()
    }
}