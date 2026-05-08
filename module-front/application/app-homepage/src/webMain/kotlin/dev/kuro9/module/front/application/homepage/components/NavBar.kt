package dev.kuro9.module.front.application.homepage.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteState
import dev.kuro9.module.front.application.homepage.state.user.UserEffect
import dev.kuro9.module.front.application.homepage.state.user.UserState
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun NavBar() {
    Div(attrs = {
        style {
            padding(12.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(Color("#333333"))
            }
            backgroundColor(Color("#1e1e1e"))
            color(Color("#f1f1f1"))
            fontFamily("serif")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
        }
    }) {
        Logo()
        NavMenus()
        UtilButtons()
    }
}

@Composable
private fun Logo() {
    Div(attrs = {
        style {
            flex(1)
            fontSize(24.px)
        }
    }) {
        Text("kuro9.dev")
    }
}

@Composable
private fun NavMenus() {
    val routeState: RouteState = koinInject()
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            gap(6.px)
        }
    }) {
        MenuItem(routeState.nowPage == Route.HOME, "Home") { routeState.navigate(Route.HOME) }
        MenuItem(routeState.nowPage == Route.ABOUT, "About") { routeState.navigate(Route.ABOUT) }
        MenuItem(routeState.nowPage == Route.CONTACT, "Contact") { routeState.navigate(Route.CONTACT) }
        MenuItem(routeState.nowPage == Route.Services.ROOT, "Services") { routeState.navigate(Route.Services.ROOT) }
    }
}

@Composable
private fun UtilButtons() {
    val routeState: RouteState = koinInject()
    val userViewModel: UserViewModel = koinInject()
    val userState: UserState = koinInject()
    LaunchedEffect(Unit) {
        launch {
            userViewModel.effect.collect { effect ->
                println("effect: $effect")
                when (effect) {
                    is UserEffect.OpenOAuth -> {
                        println("openOAuth: $effect")
                        window.location.href = effect.url
                    }
                }
            }
        }
        userViewModel.refreshMyInfo()
    }

    Div(attrs = {
        style {
            flex(1)
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.FlexEnd)
        }
    }) {
        Button(attrs = {
            style {
                cursor("pointer")
                padding(4.px, 12.px)
                border {
                    width(1.px)
                    style(LineStyle.Solid)
                    color(Color("#444444"))
                }
                backgroundColor(Color("#2a2a2a"))
                color(Color("#f1f1f1"))
                fontFamily("serif")
                fontSize(16.px)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
                lineHeight("1")
            }

            onClick {
                println("login button clicked, userInfo: ${userState.userInfo}")
                when (userState.userInfo) {
                    null -> userViewModel.doLogin()

                    else -> routeState.navigate(Route.PROFILE)
                }
            }
        }) {
            when (userState.userInfo) {
                null -> Text("Login")
                else -> {
                    userState.userInfo?.userAvatarUrl?.let { avatarUrl ->
                        Img(
                            src = avatarUrl,
                            attrs = {
                                style {
                                    width(28.px)
                                    height(28.px)
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
                        Span(attrs = {
                            style {
                                display(DisplayStyle.Flex)
                                alignItems(AlignItems.Center)
                                lineHeight("1")
                            }
                        }) {
                            Text("Logged in as : ${userState.userInfo!!.userName}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(isSelected: Boolean, text: String, doNavigate: () -> Unit) {
    A(attrs = {
        style {
            cursor("pointer")
            padding(3.px, 8.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(Color("#444444"))
            }
            textDecoration("none")
            fontFamily("serif")
            fontSize(16.px)

            if (isSelected) {
                backgroundColor(Color("#3a3a3a"))
                color(Color("#ffffff"))
                fontWeight("bold")
            } else {
                backgroundColor(Color("#242424"))
                color(Color("#9ecbff"))
            }
        }

        onClick {
            println("goto $text")
            doNavigate()
        }
    }) {
        Text(text)
    }
}