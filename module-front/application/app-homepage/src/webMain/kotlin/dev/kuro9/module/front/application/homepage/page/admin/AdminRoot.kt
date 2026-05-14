package dev.kuro9.module.front.application.homepage.page.admin

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.dom.*

@Composable
fun AdminRoot(routeState: RouteViewModel) {
    H3 { Text("어드민 메뉴") }
    Hr()
    Ul {
        Li {
            A(attrs = { onClick { routeState.navigate(Route.Admin.RESOURCE_MANAGE) } }) {
                Text("리소스 관리")
            }
        }
    }
}
