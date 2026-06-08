package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun MahjongServerSelectPage(routeState: RouteViewModel) {
    val mahjongViewModel: MahjongViewModel = koinInject()
    val servers = mahjongViewModel.state.servers

    LaunchedEffect(Unit) {
        mahjongViewModel.updateServers()
    }

    Div(attrs = {
        style {
            padding(20.px)
            color(Color("#f1f1f1"))
        }
    }) {
        H2 { Text("마작 서버 선택") }

        if (servers.isEmpty()) {
            P { Text("서버가 없습니다.") }
        } else {
            Ul {
                servers.forEach { server ->
                    Li(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            gap(10.px)
                            marginBottom(10.px)
                        }
                    }) {
                        server.iconUrl?.let {
                            Img(src = it, alt = server.name, attrs = {
                                style {
                                    width(32.px)
                                    height(32.px)
                                    borderRadius(50.percent)
                                }
                            })
                        }
                        A(href = "#", attrs = {
                            onClick {
                                it.preventDefault()
                                routeState.navigate(Route.Services.MahjongServer(server.id))
                            }
                            style {
                                color(Color("#3498db"))
                                cursor("pointer")
                                property("text-decoration", "underline")
                            }
                        }) {
                            Text(server.name)
                        }
                    }
                }
            }
        }
    }
}
