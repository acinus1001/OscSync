package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Text

@Composable
fun MahjongLayout(
    serverId: String,
    routeState: RouteViewModel,
    content: @Composable () -> Unit
) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            height(100.vh)
            color(Color("#f1f1f1"))
        }
    }) {
        // 왼쪽 메뉴바
        Nav(attrs = {
            style {
                width(200.px)
                backgroundColor(Color("#2d2d2d"))
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                padding(10.px)
                gap(5.px)
                property("border-right", "1px solid #444")
            }
        }) {
            H3(attrs = {
                style {
                    fontSize(1.2.em)
                    marginBottom(15.px)
                    paddingLeft(10.px)
                }
            }) { Text("마작 메뉴") }

            MahjongMenuItem("대국 기록", routeState.nowPage is Route.Services.MahjongRecords) {
                routeState.navigate(Route.Services.MahjongRecords(serverId))
            }
            MahjongMenuItem("통계", routeState.nowPage is Route.Services.MahjongStats) {
                routeState.navigate(Route.Services.MahjongStats(serverId))
            }
            MahjongMenuItem("순위", routeState.nowPage is Route.Services.MahjongRanks) {
                routeState.navigate(Route.Services.MahjongRanks(serverId))
            }
        }

        // 메인 콘텐츠 영역
        Div(attrs = {
            style {
                flex(1)
                padding(20.px)
                overflowY("auto")
            }
        }) {
            content()
        }
    }
}

@Composable
private fun MahjongMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Div(attrs = {
        onClick { onClick() }
        style {
            padding(10.px, 15.px)
            if (isSelected) {
                backgroundColor(Color("#3e444a"))
                fontWeight("bold")
                color(Color("#3498db"))
            } else {
                color(Color("#adb5bd"))
            }
            cursor("pointer")
            borderRadius(4.px)

            property("transition", "background-color 0.2s")
        }
    }) {
        Text(text)
    }
}
