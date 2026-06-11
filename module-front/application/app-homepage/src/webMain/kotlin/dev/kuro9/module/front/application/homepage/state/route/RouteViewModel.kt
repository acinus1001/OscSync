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
        object MAHJONG : Route("/services/mahjong"), Services
        class MahjongServer(val serverId: Long) : Route("/services/mahjong/guilds/$serverId"), Services
        class MahjongRecords(val serverId: Long) : Route("/services/mahjong/guilds/$serverId/records"), Services
        class MahjongStats(val serverId: Long) : Route("/services/mahjong/guilds/$serverId/stats"), Services
        class MahjongRanks(val serverId: Long) : Route("/services/mahjong/guilds/$serverId/ranks"), Services
        class MahjongRecordDetail(val serverId: Long, val recordId: Long) :
            Route("/services/mahjong/guilds/$serverId/records/$recordId"), Services

        class MahjongUserStats(val serverId: Long, val userId: Long) :
            Route("/services/mahjong/guilds/$serverId/stats/$userId"), Services
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
                path.startsWith("/services") -> {
                    if (path == "/services/iot") return Services.IOT
                    if (path == "/services/mahjong") return Services.MAHJONG

                    val mahjongRegex =
                        Regex("^/services/mahjong/guilds/([^/]+)(?:/(records|stats|ranks)(?:/([^/]+))?)?/?$")
                    val matchResult = mahjongRegex.find(path)

                    if (matchResult != null) {
                        val serverId = matchResult.groupValues[1].toLong()
                        val subPath = matchResult.groupValues[2]
                        val detailId = matchResult.groupValues[3]

                        return when (subPath) {
                            "records" -> {
                                if (detailId.isNotEmpty()) Services.MahjongRecordDetail(serverId, detailId.toLong())
                                else Services.MahjongRecords(serverId)
                            }

                            "stats" -> {
                                if (detailId.isNotEmpty()) Services.MahjongUserStats(serverId, detailId.toLong())
                                else Services.MahjongStats(serverId)
                            }

                            "ranks" -> Services.MahjongRanks(serverId)
                            else -> Services.MahjongServer(serverId)
                        }
                    }
                }

                path.startsWith("/admin") -> when (path) {
                    "/admin/resource-manage" -> return Admin.RESOURCE_MANAGE
                }
            }

            return null
        }
    }
}