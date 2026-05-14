package dev.kuro9.module.front.application.homepage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.kuro9.module.front.application.homepage.components.NavBar
import dev.kuro9.module.front.application.homepage.page.About
import dev.kuro9.module.front.application.homepage.page.Contact
import dev.kuro9.module.front.application.homepage.page.Index
import dev.kuro9.module.front.application.homepage.page.Profile
import dev.kuro9.module.front.application.homepage.page.admin.AdminRouter
import dev.kuro9.module.front.application.homepage.page.services.ServicesRouter
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import org.jetbrains.compose.web.dom.Hr
import org.koin.compose.koinInject

@Composable
fun App() {
    val routeState: RouteViewModel = koinInject()
    val userViewModel: UserViewModel = koinInject()
    
    LaunchedEffect(Unit) {
        userViewModel.refreshMyInfo()
    }

    NavBar()
    Hr()

    when (routeState.nowPage) {
        Route.HOME -> Index()
        Route.ABOUT -> About()
        Route.CONTACT -> Contact()
        is Route.Services -> ServicesRouter(routeState)
        is Route.Admin -> AdminRouter(routeState)
        Route.PROFILE -> Profile()
    }
}