package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.MahjongApiService
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject

@Composable
fun MahjongRecordsPage(serverId: String, routeState: RouteViewModel) {
    val mahjongApiService: MahjongApiService = koinInject()
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<MahjongPagingResult<MahjongRecord>?>(null) }
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(serverId, page) {
        isLoading = true
        try {
            result = mahjongApiService.getAllRecords(serverId.toLong(), page)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    MahjongLayout(serverId, routeState) {
        H2 { Text("대국 기록 ($serverId)") }

        if (isLoading) {
            P { Text("로딩 중...") }
        } else {
            val content = result?.content ?: emptyList()
            if (content.isEmpty()) {
                P { Text("대국 기록이 없습니다.") }
            } else {
                Table(attrs = {
                    style {
                        width(100.percent)
                        property("border-collapse", "collapse")
                        marginTop(20.px)
                    }
                }) {
                    Thead {
                        Tr {
                            Th { Text("날짜") }
                            Th { Text("결과") }
                            Th { Text("상세") }
                        }
                    }
                    Tbody {
                        content.forEach { record ->
                            Tr {
                                Td(attrs = {
                                    style {
                                        padding(8.px); textAlign("center"); border(
                                        1.px,
                                        LineStyle.Solid,
                                        Color("#444")
                                    )
                                    }
                                }) {
                                    Text(record.createdAt.date.toString())
                                }
                                Td(attrs = { style { padding(8.px); border(1.px, LineStyle.Solid, Color("#444")) } }) {
                                    Div(attrs = {
                                        style {
                                            display(DisplayStyle.Flex)
                                            justifyContent(JustifyContent.Center)
                                            gap(10.px)
                                        }
                                    }) {
                                        record.userScores.sortedBy { it.rank }.forEach { score ->
                                            Span {
                                                Text("${score.userName}: ${score.score} (${score.pointDeltaStringified})")
                                            }
                                        }
                                    }
                                }
                                Td(attrs = {
                                    style {
                                        padding(8.px); textAlign("center"); border(
                                        1.px,
                                        LineStyle.Solid,
                                        Color("#444")
                                    )
                                    }
                                }) {
                                    Button(attrs = {
                                        onClick {
                                            routeState.navigate(
                                                dev.kuro9.module.front.application.homepage.state.route.Route.Services.MahjongRecordDetail(
                                                    serverId,
                                                    record.id.toString()
                                                )
                                            )
                                        }
                                    }) {
                                        Text("보기")
                                    }
                                }
                            }
                        }
                    }
                }

                // Simple Pagination
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        alignItems(AlignItems.Center)
                        gap(20.px)
                        marginTop(20.px)
                    }
                }) {
                    Button(attrs = {
                        if (page <= 1) disabled()
                        onClick { if (page > 1) page-- }
                    }) {
                        Text("이전")
                    }
                    Text("페이지 $page / ${result?.maxPage ?: 1}")
                    Button(attrs = {
                        if (page >= (result?.maxPage ?: 1)) disabled()
                        onClick { if (page < (result?.maxPage ?: 1)) page++ }
                    }) {
                        Text("다음")
                    }
                }
            }
        }
    }
}

@Composable
fun MahjongStatsPage(serverId: String, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("통계 ($serverId)") }
        P { Text("서버 통계가 여기에 표시됩니다.") }
    }
}

@Composable
fun MahjongRanksPage(serverId: String, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("랭킹 ($serverId)") }
        P { Text("유저 랭킹이 여기에 표시됩니다.") }
    }
}

@Composable
fun MahjongRecordDetailPage(serverId: String, recordId: String, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("대국 상세 기록 ($serverId - $recordId)") }
        P { Text("대국 ID $recordId 에 대한 상세 정보가 여기에 표시됩니다.") }
    }
}

@Composable
fun MahjongUserStatsPage(serverId: String, userId: String, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("사용자 통계 ($serverId - $userId)") }
        P { Text("사용자 $userId 에 대한 상세 통계가 여기에 표시됩니다.") }
    }
}
