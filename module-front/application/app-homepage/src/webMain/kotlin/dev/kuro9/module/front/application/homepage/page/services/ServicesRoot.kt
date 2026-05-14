package dev.kuro9.module.front.application.homepage.page.services

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.state.user.UserState
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun ServicesRoot(routeState: RouteViewModel) {
    val userState: UserState = koinInject()

    H3 { Text("서비스 목록") }
    Hr()
    Ul {
        Li {
            A(attrs = {
                onClick {
                    if (userState.hasIotRule()) {
                        routeState.navigate(Route.Services.IOT)
                        return@onClick
                    }

                    if (userState.userInfo == null) {
                        window.alert("로그인이 필요합니다.")
                        return@onClick
                    }

                    window.alert("필요 권한이 없습니다. 권한이 있다고 생각되는 경우 재로그인 해 주십시오.")
                    return@onClick
                }
            }) {
                Text("${if (userState.hasIotRule()) "" else "[권한 필요] "}내방 조명 스위치")
            }
        }
    }

}

private fun UserState.hasIotRule(): Boolean {
    return this.userInfo?.authorities?.any { it in listOf("ROLE_ROOT", "AUTHORITY_HOMEPAGE_IOT") } ?: false
}