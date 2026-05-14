package dev.kuro9.module.front.application.homepage.page.admin

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.admin.resource_manage.AdminResourceManage
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel

@Composable
fun AdminRouter(routeViewModel: RouteViewModel) {
    when (routeViewModel.nowPage) {
        Route.Admin.ROOT -> AdminRoot(routeViewModel)
        Route.Admin.RESOURCE_MANAGE -> AdminResourceManage()
        !is Route.Admin -> return
    }
}
