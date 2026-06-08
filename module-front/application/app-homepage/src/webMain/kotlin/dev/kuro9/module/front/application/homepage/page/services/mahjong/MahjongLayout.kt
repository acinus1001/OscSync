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
                backgroundColor(Color("#2c3e50"))
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                padding(10.px)
                gap(10.px)
            }
        }) {
            H3 { Text("서버: $serverId") }

            MahjongMenuItem("대국 기록", routeState.nowPage is Route.Services.MahjongRecords) {
                routeState.navigate(Route.Services.MahjongRecords(serverId))
            }
            MahjongMenuItem("통계", routeState.nowPage is Route.Services.MahjongStats) {
                routeState.navigate(Route.Services.MahjongStats(serverId))
            }
            MahjongMenuItem("랭킹", routeState.nowPage is Route.Services.MahjongRanks) {
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
            padding(10.px)
            if (isSelected) backgroundColor(Color("#34495e"))
            cursor("pointer")
            borderRadius(4.px)
            // Hover 효과는 복잡하므로 여기서는 생략하거나 인라인 스타일로 처리
        }
    }) {
        Text(text)
    }
}
