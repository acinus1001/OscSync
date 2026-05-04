package dev.kuro9.module.front.application.homepage.components

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.Route
import dev.kuro9.module.front.application.homepage.RouteState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun NavBar() {
    val routeState: RouteState = koinInject()
    Div(attrs = {
        style {
            padding(12.px)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(Color.black)
            }
            backgroundColor(Color("#eeeeee"))
            fontFamily("serif")
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
        }
    }) {
        Div(attrs = {
            style {
                fontSize(24.px)
                fontWeight("bold")
                marginBottom(8.px)
            }
        }) {
            Text("kuro9.dev")
        }

        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                gap(6.px)
            }
        }) {
            MenuItem(routeState.nowPage == Route.HOME, "Home") { routeState.navigate(Route.HOME) }
            MenuItem(routeState.nowPage == Route.ABOUT, "About") { routeState.navigate(Route.ABOUT) }
            MenuItem(routeState.nowPage == Route.CONTACT, "Contact") { routeState.navigate(Route.CONTACT) }
            MenuItem(routeState.nowPage == Route.SERVICES, "Services") { routeState.navigate(Route.SERVICES) }
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
                color(Color.black)
            }
            textDecoration("none")
            fontFamily("serif")
            fontSize(16.px)

            if (isSelected) {
                backgroundColor(Color("#cccccc"))
                color(Color.black)
                fontWeight("bold")
            } else {
                backgroundColor(Color("#f8f8f8"))
                color(Color("#0000ee"))
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