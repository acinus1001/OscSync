package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteState
import dev.kuro9.module.front.application.homepage.state.user.UserState
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun Profile() {
    val routeState: RouteState = koinInject()
    val userState: UserState = koinInject()
    val userViewModel: UserViewModel = koinInject()
    val scope = rememberCoroutineScope()
    val userInfo = userState.userInfo

    if (userInfo == null) {
        routeState.navigate(Route.HOME)
        return
    }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            gap(16.px)
            padding(40.px)
        }
    }) {
        Img(
            src = userInfo.userAvatarUrl ?: "",
            attrs = {
                style {
                    width(120.px)
                    height(120.px)
                    borderRadius(50.percent)
                    property("object-fit", "cover")
                    border {
                        width(1.px)
                        style(LineStyle.Solid)
                        color(Color("#555555"))
                    }
                }
            }
        )

        Div(attrs = {
            style {
                fontSize(24.px)
                fontWeight("bold")
                color(Color("#f1f1f1"))
            }
        }) {
            Text(userInfo.userName)
        }

        Button(attrs = {
            style {
                cursor("pointer")
                padding(8.px, 20.px)
                margin(8.px)
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(Color("#444444"))
                }
                color(Color("#f1f1f1"))
                property("background-color", "#2a2a2a")
                fontSize(16.px)
            }

            onClick {
                scope.launch {
                    userViewModel.doLogout()
                }
            }
        }) {
            Text("Logout")
        }
    }

}