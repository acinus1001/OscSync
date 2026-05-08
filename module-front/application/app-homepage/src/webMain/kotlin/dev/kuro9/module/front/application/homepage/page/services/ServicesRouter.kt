package dev.kuro9.module.front.application.homepage.page.services

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.services.iot.IotRoot
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteState

@Composable
fun ServicesRouter(routeState: RouteState) {
    when (routeState.nowPage) {
        Route.Services.ROOT -> ServicesRoot(routeState)
        Route.Services.IOT -> IotRoot()
        !is Route.Services -> return
    }
}