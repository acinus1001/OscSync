package dev.kuro9.module.front.application.homepage.page.admin

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.admin.resource_manage.AdminResourceManage
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.state.user.UserState
import kotlinx.browser.window
import org.koin.compose.koinInject

@Composable
fun AdminRouter(routeViewModel: RouteViewModel) {
    val userState: UserState = koinInject()

    run {
        if (userState.userInfo?.authorities?.contains("ROLE_ROOT") ?: false) {
            return@run
        }

        if (userState.userInfo == null) {
            window.alert("로그인이 필요합니다.")
            routeViewModel.navigate(Route.HOME)
            return
        }

        window.alert("필요 권한이 없습니다. 권한이 있다고 생각되는 경우 재로그인 해 주십시오.")
        routeViewModel.navigate(Route.HOME)
        return
    }

    when (routeViewModel.nowPage) {
        Route.Admin.ROOT -> AdminRoot(routeViewModel)
        Route.Admin.RESOURCE_MANAGE -> AdminResourceManage()
        !is Route.Admin -> return
    }
}
