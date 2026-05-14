package dev.kuro9.module.front.application.homepage.state.route

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import kotlinx.browser.window
import kotlinx.coroutines.launch

class RouteViewModel(private val userViewModel: UserViewModel) : ViewModel() {
    var nowPage: Route by mutableStateOf(Route.fromPath(window.location.pathname).also { println("init : $it") }
        ?: Route.HOME)
        private set;

    init {
        window.onpopstate = {
            nowPage = Route.fromPath(window.location.pathname) ?: Route.HOME
        }
    }

    fun navigate(route: Route) {
        window.history.pushState(null, "", route.path)
        nowPage = route

        // 페이지 이동 때마다 현재 권한 새로고침하기
        // userViewModel 종속되는게 좀 그런데 이벤트 기반으로 할 수 있으면 추후 변경하는게 좋을지도
        viewModelScope.launch {
            userViewModel.refreshMyInfo()
        }
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

    sealed interface Admin {
        object ROOT : Route("/admin"), Admin
        object RESOURCE_MANAGE : Route("/admin/resource-manage"), Admin
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
                "/admin" -> return Admin.ROOT
            }

            when {
                path.startsWith("/services") -> when (path) {
                    "/services/iot" -> return Services.IOT
                }

                path.startsWith("/admin") -> when (path) {
                    "/admin/resource-manage" -> return Admin.RESOURCE_MANAGE
                }
            }

            return null
        }
    }
}