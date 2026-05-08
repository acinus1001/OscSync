package dev.kuro9.module.front.application.homepage.state.route

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window

class RouteState {
    var nowPage: Route by mutableStateOf(Route.fromPath(window.location.pathname).also { println("init : $it") }
        ?: Route.HOME)
        private set;

    fun navigate(route: Route) {
        window.history.pushState(null, "", route.path)
        nowPage = route
    }
}

sealed class Route(val path: String) {
    object HOME : Route("/")
    object ABOUT : Route("/about")
    object CONTACT : Route("/contact")
    sealed interface Services {
        object ROOT : Route("/services"), Services
        object IOT : Route("/services/iot"), Services
    }

    object PROFILE : Route("/profile")

    companion object {
        fun fromPath(path: String): Route? {
            when (path) {
                "/" -> return HOME
                "/about" -> return ABOUT
                "/contact" -> return CONTACT
                "/services" -> return Services.ROOT
                "/profile" -> return PROFILE
            }

            when {
                path.startsWith("/services") -> when (path) {
                    "/services/iot" -> return Services.IOT
                }
            }

            return null
        }
    }
}