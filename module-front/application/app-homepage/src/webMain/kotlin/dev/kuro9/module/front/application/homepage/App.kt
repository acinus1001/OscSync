package dev.kuro9.module.front.application.homepage

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.components.NavBar
import dev.kuro9.module.front.application.homepage.page.About
import dev.kuro9.module.front.application.homepage.page.Contact
import dev.kuro9.module.front.application.homepage.page.Index
import dev.kuro9.module.front.application.homepage.page.Profile
import dev.kuro9.module.front.application.homepage.page.admin.AdminRouter
import dev.kuro9.module.front.application.homepage.page.services.ServicesRouter
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.koin.compose.koinInject

@Composable
fun App() {
    val routeState: RouteViewModel = koinInject()
    NavBar()

    Div(attrs = {
        style {
            paddingTop(80.px) // NavBar height adjustment
            maxWidth(1200.px)
            property("margin", "0 auto")
            property("padding-left", "20px")
            property("padding-right", "20px")
        }
    }) {
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
}