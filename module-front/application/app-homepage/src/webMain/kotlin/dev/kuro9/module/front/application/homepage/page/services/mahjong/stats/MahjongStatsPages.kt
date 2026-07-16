package dev.kuro9.module.front.application.homepage.page.services.mahjong.stats

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.DiscordNameApiService
import dev.kuro9.module.front.application.homepage.network.MahjongApiService
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongState
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongViewModel
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordIdAndName
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongGuildStat
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongUserStat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MahjongStatsPage(serverId: Long, routeState: RouteViewModel) {
    val mahjongApiService: MahjongApiService = koinInject()
    var guildStat by remember { mutableStateOf<MahjongGuildStat?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(serverId) {
        isLoading = true
        try {
            guildStat = mahjongApiService.getGuildStat(serverId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    MahjongLayout(serverId, routeState) {
        H2 { Text("서버 통계") }

        if (isLoading) {
            P { Text("로딩 중...") }
        } else {
            guildStat?.let { stat ->
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(20.px)
                    }
                }) {
                    // 기본 정보
                    Div(attrs = {
                        style {
                            padding(20.px)
                            backgroundColor(Color("#333"))
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, Color("#444"))
                        }
                    }) {
                        H3 { Text("전체 요약") }
                        P { Text("총 대국 수: ${stat.totalGameCount}") }
                        stat.highScore?.let { score ->
                            P {
                                Text("최고 점수: $score")
                                stat.highScoreGameId?.let { gameId ->
                                    Span(attrs = {
                                        style {
                                            marginLeft(10.px)
                                            fontSize(0.8.em)
                                            color(Color("#3498db"))
                                            cursor("pointer")
                                            property("text-decoration", "underline")
                                        }
                                        onClick {
                                            routeState.navigate(Route.Services.MahjongRecordDetail(serverId, gameId))
                                        }
                                    }) { Text("(기록 보기)") }
                                }
                            }
                        }
                        stat.lowScore?.let { score ->
                            P {
                                Text("최저 점수: $score")
                                stat.lowScoreGameId?.let { gameId ->
                                    Span(attrs = {
                                        style {
                                            marginLeft(10.px)
                                            fontSize(0.8.em)
                                            color(Color("#3498db"))
                                            cursor("pointer")
                                            property("text-decoration", "underline")
                                        }
                                        onClick {
                                            routeState.navigate(Route.Services.MahjongRecordDetail(serverId, gameId))
                                        }
                                    }) { Text("(기록 보기)") }
                                }
                            }
                        }
                    }

                    // 월별 대국 수
                    Div(attrs = {
                        style {
                            padding(20.px)
                            backgroundColor(Color("#333"))
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, Color("#444"))
                        }
                    }) {
                        H3 { Text("월별 대국 수") }
                        Table(attrs = {
                            style {
                                width(100.percent)
                                property("border-collapse", "collapse")
                                textAlign("center")
                            }
                        }) {
                            Thead {
                                Tr {
                                    listOf("년월", "대국 수").forEach {
                                        Th(attrs = {
                                            style {
                                                padding(10.px)
                                                backgroundColor(Color("#444"))
                                                property("border", "1px solid #555")
                                            }
                                        }) { Text(it) }
                                    }
                                }
                            }
                            Tbody {
                                stat.gameCountPerMonthDescending.forEach { (yearMonth, count) ->
                                    Tr {
                                        Td(attrs = {
                                            style {
                                                padding(10.px)
                                                property("border", "1px solid #555")
                                            }
                                        }) { Text(yearMonth.toString()) }
                                        Td(attrs = {
                                            style {
                                                padding(10.px)
                                                property("border", "1px solid #555")
                                            }
                                        }) { Text(count.toString()) }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: P { Text("통계 데이터를 불러올 수 없습니다.") }
        }
    }
}

@Composable
fun MahjongUserStatsPage(serverId: Long, userId: Long, routeState: RouteViewModel) {
    val mahjongApiService: MahjongApiService = koinInject()
    val discordNameApiService: DiscordNameApiService = koinInject()
    val mahjongViewModel: MahjongViewModel = koinInject()
    val searchState = mahjongViewModel.state

    val scope = rememberCoroutineScope()
    var userStat by remember { mutableStateOf<MahjongUserStat?>(null) }
    var guildStat by remember { mutableStateOf<MahjongGuildStat?>(null) }
    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var autocompleteList by remember { mutableStateOf<List<DiscordIdAndName>>(emptyList()) }
    var isAutocompleteOpen by remember { mutableStateOf(false) }
    var autocompleteJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(serverId, userId, selectedMonth) {
        isLoading = true
        try {
            if (guildStat == null || guildStat?.guildId != serverId) {
                guildStat = mahjongApiService.getGuildStat(serverId)
            }

            userStat = if (selectedMonth == null) {
                mahjongApiService.getUserStat(serverId, userId)
            } else {
                mahjongApiService.getUserStatByYearMonth(serverId, userId, selectedMonth!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            userStat = null
        } finally {
            isLoading = false
        }
    }

    MahjongLayout(serverId, routeState) {
        H2 { Text("개인 통계") }

        // 유저 검색 및 월 선택 필터
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(10.px)
                marginBottom(20.px)
                padding(15.px)
                backgroundColor(Color("#333"))
                property("border", "1px solid #444")
            }
        }) {
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(15.px)
                    flexWrap(FlexWrap.Wrap)
                }
            }) {
                // 유저 검색 (Autocomplete)
                Div(attrs = {
                    style {
                        position(Position.Relative)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                    }
                }) {
                    Select(attrs = {
                        onChange {
                            searchState.searchMode = MahjongState.SearchMode.valueOf(it.value ?: "NAME")
                            searchState.searchUserId = null
                            searchState.searchUserName = ""
                            autocompleteList = emptyList()
                            isAutocompleteOpen = false
                        }
                        style {
                            backgroundColor(Color("#444"))
                            color(Color("#f1f1f1"))
                            border(1.px, LineStyle.Solid, Color("#555"))
                            padding(5.px)
                        }
                    }) {
                        Option("NAME") {
                            if (searchState.searchMode == MahjongState.SearchMode.NAME) {
                                DomSideEffect { it.selected = true }
                            }
                            Text("유저 이름")
                        }
                        Option("ID") {
                            if (searchState.searchMode == MahjongState.SearchMode.ID) {
                                DomSideEffect { it.selected = true }
                            }
                            Text("유저 ID")
                        }
                    }

                    Input(type = InputType.Text, attrs = {
                        placeholder(if (searchState.searchMode == MahjongState.SearchMode.NAME) "이름으로 검색..." else "ID 직접 입력...")
                        value(searchState.searchUserName)
                        onInput {
                            val keyword = it.value
                            searchState.searchUserName = keyword

                            if (searchState.searchMode == MahjongState.SearchMode.NAME) {
                                if (keyword.isBlank()) {
                                    autocompleteList = emptyList()
                                    isAutocompleteOpen = false
                                    return@onInput
                                }

                                autocompleteJob?.cancel()
                                autocompleteJob = scope.launch {
                                    delay(500.milliseconds)
                                    try {
                                        autocompleteList = discordNameApiService.searchNames(keyword)
                                        isAutocompleteOpen = autocompleteList.isNotEmpty()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                // ID 모드
                                val targetId = keyword.toLongOrNull()
                                if (targetId != null) {
                                    routeState.navigate(Route.Services.MahjongUserStats(serverId, targetId))
                                }
                            }
                        }
                        style {
                            backgroundColor(Color("#444"))
                            color(Color("#f1f1f1"))
                            border(1.px, LineStyle.Solid, Color("#555"))
                            padding(5.px)
                        }
                    })

                    // Autocomplete Dropdown
                    if (searchState.searchMode == MahjongState.SearchMode.NAME && isAutocompleteOpen) {
                        Ul(attrs = {
                            style {
                                position(Position.Absolute)
                                property("top", "100%")
                                left(100.px)
                                right(0.px)
                                backgroundColor(Color("#222"))
                                property("z-index", "2000")
                                property("border", "1px solid #555")
                                property("list-style", "none")
                                padding(0.px)
                                margin(0.px)
                                maxHeight(200.px)
                                overflowY("auto")
                            }
                        }) {
                            autocompleteList.forEach { user ->
                                Li(attrs = {
                                    onClick {
                                        searchState.searchUserName = user.name
                                        isAutocompleteOpen = false
                                        routeState.navigate(Route.Services.MahjongUserStats(serverId, user.id))
                                    }
                                    onMouseEnter {
                                        (it.target as? HTMLElement)?.style?.backgroundColor = "#444"
                                    }
                                    onMouseLeave {
                                        (it.target as? HTMLElement)?.style?.backgroundColor = "transparent"
                                    }
                                    style {
                                        padding(8.px, 12.px)
                                        cursor("pointer")
                                        property("border-bottom", "1px solid #444")
                                    }
                                }) {
                                    Text(user.name)
                                }
                            }
                        }
                    }
                }

                // 기간 선택 (월 선택)
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                    }
                }) {
                    Label {
                        Text("기간: ")
                        Select(attrs = {
                            onChange {
                                selectedMonth = if (it.value == "TOTAL") null else it.value
                            }
                            style {
                                backgroundColor(Color("#444"))
                                color(Color("#f1f1f1"))
                                border(1.px, LineStyle.Solid, Color("#555"))
                                padding(5.px)
                            }
                        }) {
                            Option("TOTAL") {
                                if (selectedMonth == null) {
                                    DomSideEffect { it.selected = true }
                                }
                                Text("전체 기간")
                            }
                            guildStat?.gameCountPerMonthDescending?.forEach { (yearMonth, _) ->
                                val ymStr = yearMonth.toString()
                                Option(ymStr) {
                                    if (selectedMonth == ymStr) {
                                        DomSideEffect { it.selected = true }
                                    }
                                    Text(ymStr)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            P { Text("로딩 중...") }
        } else {
            userStat?.let { stat ->
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(25.px)
                    }
                }) {
                    // 상단 요약 정보 카드
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            gap(20.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        listOf(
                            "우마 합계" to stat.totalUmaSumString,
                            "우마 순위" to "${stat.umaRank}위",
                            "대국 수" to "${stat.totalGameCount}회",
                            "대국 순위" to "${stat.gameCountRank}위",
                            "평균 순위" to stat.avgPlaceString,
                            "평균 우마" to stat.avgUmaString
                        ).forEach { (label, value) ->
                            Div(attrs = {
                                style {
                                    flex(1)
                                    minWidth(120.px)
                                    padding(15.px)
                                    backgroundColor(Color("#333"))
                                    borderRadius(8.px)
                                    textAlign("center")
                                    border(1.px, LineStyle.Solid, Color("#444"))
                                }
                            }) {
                                Div(attrs = { style { fontSize(0.8.em); color(Color("#bbb")); marginBottom(5.px) } }) {
                                    Text(
                                        label
                                    )
                                }
                                Div(attrs = { style { fontSize(1.2.em); fontWeight("bold"); color(Color("#3498db")) } }) {
                                    Text(
                                        value
                                    )
                                }
                            }
                        }
                    }

                    // 순위 분포 및 이미지
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            gap(20.px)
                            flexWrap(FlexWrap.Wrap)
                        }
                    }) {
                        // 순위 테이블
                        Div(attrs = {
                            style {
                                flex(1)
                                minWidth(300.px)
                                padding(20.px)
                                backgroundColor(Color("#333"))
                                borderRadius(8.px)
                                border(1.px, LineStyle.Solid, Color("#444"))
                            }
                        }) {
                            H3 { Text("순위 분포") }
                            Table(attrs = {
                                style {
                                    width(100.percent)
                                    property("border-collapse", "collapse")
                                    textAlign("center")
                                }
                            }) {
                                Thead {
                                    Tr {
                                        listOf("순위", "횟수", "비율").forEach {
                                            Th(attrs = {
                                                style {
                                                    padding(10.px)
                                                    property("border-bottom", "1px solid #555")
                                                }
                                            }) { Text(it) }
                                        }
                                    }
                                }
                                Tbody {
                                    listOf(
                                        Triple("1위", stat.firstPlaceCount, stat.firstPlaceRateString),
                                        Triple("2위", stat.secondPlaceCount, stat.secondPlaceRateString),
                                        Triple("3위", stat.thirdPlaceCount, stat.thirdPlaceRateString),
                                        Triple("4위", stat.fourthPlaceCount, stat.fourthPlaceRateString),
                                        Triple("토비", stat.tobiCount, stat.tobiRateString)
                                    ).forEach { (label, count, rate) ->
                                        Tr {
                                            Td(attrs = { style { padding(10.px) } }) { Text(label) }
                                            Td(attrs = { style { padding(10.px) } }) { Text("${count}회") }
                                            Td(attrs = { style { padding(10.px); fontWeight("bold") } }) { Text("$rate%") }
                                        }
                                    }
                                }
                            }
                        }

                        // 최근 10국 추이 그래프
                        Div(attrs = {
                            style {
                                flex(1)
                                minWidth(300.px)
                                padding(20.px)
                                backgroundColor(Color("#333"))
                                borderRadius(8.px)
                                border(1.px, LineStyle.Solid, Color("#444"))
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                alignItems(AlignItems.Center)
                            }
                        }) {
                            H3 { Text("최근 10국 그래프") }
                            Img(src = stat.graphImgUrl, attrs = {
                                style {
                                    maxWidth(100.percent)
                                    marginTop(10.px)
                                    borderRadius(4.px)
                                }
                            })
                        }
                    }
                }
            } ?: P { Text("사용자 통계 데이터를 찾을 수 없습니다.") }
        }
    }
}
