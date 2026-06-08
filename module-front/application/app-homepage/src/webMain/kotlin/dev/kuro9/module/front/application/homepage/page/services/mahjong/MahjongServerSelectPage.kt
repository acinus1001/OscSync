package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun MahjongServerSelectPage(routeState: RouteViewModel) {
    data class MahjongServerInfo(val id: String, val name: String, val iconUrl: String?)

    val servers = listOf(
        MahjongServerInfo(
            id = "588993828309041153",
            name = "test",
            iconUrl = null
        )
    )

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
