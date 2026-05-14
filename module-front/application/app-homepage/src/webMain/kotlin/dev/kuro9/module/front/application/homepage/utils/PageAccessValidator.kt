package dev.kuro9.module.front.application.homepage.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.state.user.UserState
import kotlinx.browser.window
import org.koin.compose.koinInject

@Composable
fun requireAnyAuthority(
    route: RouteViewModel,
    user: UserState,
    vararg authority: String,
    content: @Composable (isLoading: Boolean) -> Unit
) {
    if (user.userInfo == null) {
        window.alert("로그인이 필요합니다.")
        route.navigate(Route.HOME)
        return
    }

    val hasAuthority =
        "ROLE_ROOT" in user.userInfo!!.authorities ||
                authority.any { it in user.userInfo!!.authorities }

    if (!hasAuthority) {
        LaunchedEffect(Unit) {
            window.alert("권한이 없습니다. 권한이 있다고 생각되는 경우 재로그인 또는 관리자에게 문의 바랍니다.")
            route.navigate(Route.HOME)
        }
        return
    }

    content(user.isLoaded.not())
}

@Composable
fun requireAnyAuthority(vararg authority: String, content: @Composable (isLoading: Boolean) -> Unit) =
    requireAnyAuthority(
        route = koinInject(),
        user = koinInject(),
        authority = authority,
        content = content,
    )