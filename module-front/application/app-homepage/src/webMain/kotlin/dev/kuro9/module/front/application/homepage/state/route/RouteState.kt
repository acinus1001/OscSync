package dev.kuro9.module.front.application.homepage.state.route

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window

class RouteState {
    var nowPage: Route by mutableStateOf(Route.HOME)
        private set;

    fun navigate(route: Route) {
        window.history.pushState(null, "", route.path)
        nowPage = route
    }
}

enum class Route(val path: String) {
    HOME("/"),
    ABOUT("/about"),
    CONTACT("/contact"),
    SERVICES("/services"),
    PROFILE("/profile"),
    ;

    companion object {
        fun fromPath(path: String): Route? = entries.find { it.path == path }
    }
}