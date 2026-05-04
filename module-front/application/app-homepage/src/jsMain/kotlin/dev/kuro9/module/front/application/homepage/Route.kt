package dev.kuro9.module.front.application.homepage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window

enum class Route(val path: String) {
    HOME("/"),
    ABOUT("/about"),
    CONTACT("/contact"),
    SERVICES("/services"),
    ;

    companion object {
        fun fromPath(path: String): Route? = entries.find { it.path == path }
    }
}

class RouteState {
    var nowPage: Route by mutableStateOf(Route.HOME)
        private set;

    fun navigate(route: Route) {
        window.history.pushState(null, "", route.path)
        nowPage = route
    }
}