package dev.kuro9.module.front.application.homepage

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.components.NavBar
import dev.kuro9.module.front.application.homepage.page.About
import dev.kuro9.module.front.application.homepage.page.Contact
import dev.kuro9.module.front.application.homepage.page.Index
import dev.kuro9.module.front.application.homepage.page.Services
import org.jetbrains.compose.web.dom.Hr
import org.koin.compose.koinInject

@Composable
fun App() {
    val routeState: RouteState = koinInject()
    NavBar()
    Hr()

    when (routeState.nowPage) {
        Route.HOME -> Index()
        Route.ABOUT -> About()
        Route.CONTACT -> Contact()
        Route.SERVICES -> Services()
    }
}