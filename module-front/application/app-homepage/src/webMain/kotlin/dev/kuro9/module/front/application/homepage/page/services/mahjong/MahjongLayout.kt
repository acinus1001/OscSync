package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.GlobalStyles
import dev.kuro9.module.front.application.homepage.state.MobileMenuState
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun MahjongLayout(
    serverId: Long,
    routeState: RouteViewModel,
    content: @Composable () -> Unit
) {
    val mahjongViewModel: MahjongViewModel = koinInject()
    val servers = mahjongViewModel.state.servers
    val currentServer = servers.find { it.id == serverId }
    var isDropdownOpen by remember { mutableStateOf(false) }

    val mahjongMenu: @Composable () -> Unit = {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(5.px)
            }
        }) {
            // 서버 선택 드롭다운
            Div(attrs = {
                style {
                    position(Position.Relative)
                    marginBottom(15.px)
                }
            }) {
                Div(attrs = {
                    onClick { isDropdownOpen = !isDropdownOpen }
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(10.px)
                        padding(10.px)
                        backgroundColor(Color("#333"))
                        property("border", "1px solid #444")
                        cursor("pointer")
                        property("user-select", "none")
                    }
                }) {
                    currentServer?.iconUrl?.let {
                        Img(src = it, attrs = {
                            style {
                                width(24.px)
                                height(24.px)
                                borderRadius(50.percent)
                            }
                        })
                    } ?: Div(attrs = {
                        style {
                            width(24.px)
                            height(24.px)
                            borderRadius(50.percent)
                            backgroundColor(Color("#555"))
                        }
                    })

                    Span(attrs = {
                        style {
                            flex(1)
                            overflow("hidden")
                            property("text-overflow", "ellipsis")
                            whiteSpace("nowrap")
                            fontWeight("bold")
                            fontSize(0.9.em)
                        }
                    }) {
                        Text(currentServer?.name ?: "서버 선택")
                    }

                    Span(attrs = {
                        style {
                            fontSize(0.8.em)
                            color(Color("#888"))
                        }
                    }) { Text(if (isDropdownOpen) "▲" else "▼") }
                }

                if (isDropdownOpen) {
                    Div(attrs = {
                        style {
                            position(Position.Absolute)
                            property("top", "100%")
                            left(0.px)
                            right(0.px)
                            backgroundColor(Color("#333"))
                            property("z-index", "1000")
                            property("border", "1px solid #444")
                            property("border-top", "none")
                            overflow("hidden")
                        }
                    }) {
                        servers.forEach { server ->
                            Div(attrs = {
                                onClick {
                                    isDropdownOpen = false
                                    if (server.id != serverId) {
                                        routeState.navigate(Route.Services.MahjongServer(server.id))
                                    }
                                }
                                style {
                                    display(DisplayStyle.Flex)
                                    alignItems(AlignItems.Center)
                                    gap(10.px)
                                    padding(10.px)
                                    cursor("pointer")
                                    if (server.id == serverId) {
                                        backgroundColor(Color("#444"))
                                    }
                                    property("border-bottom", "1px solid #444")
                                }
                            }) {
                                server.iconUrl?.let {
                                    Img(src = it, attrs = {
                                        style {
                                            width(20.px)
                                            height(20.px)
                                            borderRadius(50.percent)
                                        }
                                    })
                                } ?: Div(attrs = {
                                    style {
                                        width(20.px)
                                        height(20.px)
                                        borderRadius(50.percent)
                                        backgroundColor(Color("#555"))
                                    }
                                })
                                Span(attrs = {
                                    style {
                                        fontSize(0.9.em)
                                        whiteSpace("nowrap")
                                        overflow("hidden")
                                        property("text-overflow", "ellipsis")
                                    }
                                }) {
                                    Text(server.name)
                                }
                            }
                        }
                    }
                }
            }

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
    }

    LaunchedEffect(serverId, routeState.nowPage) {
        MobileMenuState.setMenu(mahjongMenu)
    }

    DisposableEffect(Unit) {
        onDispose {
            MobileMenuState.setMenu(null)
        }
    }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            property("height", "calc(100vh - 80px)")
            color(Color("#f1f1f1"))
        }
    }) {
        // 왼쪽 메뉴바
        Nav(attrs = {
            classes(GlobalStyles.desktopOnly)
            style {
                width(200.px)
                backgroundColor(Color("#2d2d2d"))
//                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                padding(10.px)
                gap(5.px)
                property("border-right", "1px solid #444")
            }
        }) {
            mahjongMenu()
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
