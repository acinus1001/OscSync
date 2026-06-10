package dev.kuro9.module.front.application.homepage.page.services.mahjong.record

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.DiscordNameApiService
import dev.kuro9.module.front.application.homepage.network.MahjongApiService
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongState
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongViewModel
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordIdAndName
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongPagingResult
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDate
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MahjongRecordsPage(serverId: Long, routeState: RouteViewModel) {
    val mahjongApiService: MahjongApiService = koinInject()
    val discordNameApiService: DiscordNameApiService = koinInject()
    val mahjongViewModel: MahjongViewModel = koinInject()
    val searchState = mahjongViewModel.state

    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<MahjongPagingResult<MahjongRecord>?>(null) }
    var page by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }

    var autocompleteList by remember { mutableStateOf<List<DiscordIdAndName>>(emptyList()) }
    var isAutocompleteOpen by remember { mutableStateOf(false) }
    var autocompleteJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(serverId, page, searchState.searchStartDate, searchState.searchEndDate, searchState.searchUserId) {
        isLoading = true
        try {
            result = mahjongApiService.getAllRecords(
                guildId = serverId,
                page = page,
                start = searchState.searchStartDate,
                endInclusive = searchState.searchEndDate,
                userId = searchState.searchUserId
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    MahjongLayout(serverId, routeState) {
        H2 { Text("대국 기록") }
        H5 { Text("기록에 대해 상세한 정보를 보려면 기록 칸을 클릭하십시오...") }

        // 검색 필터 UI
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
                // 시작일
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                    }
                }) {
                    Label {
                        run { Text("시작일: ") }
                        Input(type = InputType.Date, attrs = {
                            value(searchState.searchStartDate?.toString() ?: "")
                            onInput {
                                val value = it.value
                                searchState.searchStartDate = if (value.isBlank()) null else value.toLocalDate()
                                page = 1
                            }
                            style {
                                backgroundColor(Color("#444"))
                                color(Color("#f1f1f1"))
                                border(1.px, LineStyle.Solid, Color("#555"))
                                padding(5.px)
                            }
                        })
                    }
                    if (searchState.searchStartDate != null) {
                        Button(attrs = {
                            onClick {
                                searchState.searchStartDate = null
                                page = 1
                            }
                            style {
                                backgroundColor(Color("#e74c3c"))
                                color(Color("#ffffff"))
                                border(0.px)
                                padding(5.px, 10.px)
                                cursor("pointer")
                                fontSize(0.8.em)
                            }
                        }) { Text("X") }
                    }
                }

                // 종료일
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(5.px)
                    }
                }) {
                    Label {
                        run { Text("종료일: ") }
                        Input(type = InputType.Date, attrs = {
                            value(searchState.searchEndDate?.toString() ?: "")
                            onInput {
                                val value = it.value
                                searchState.searchEndDate = if (value.isBlank()) null else value.toLocalDate()
                                page = 1
                            }
                            style {
                                backgroundColor(Color("#444"))
                                color(Color("#f1f1f1"))
                                border(1.px, LineStyle.Solid, Color("#555"))
                                padding(5.px)
                            }
                        })
                    }
                    if (searchState.searchEndDate != null) {
                        Button(attrs = {
                            onClick {
                                searchState.searchEndDate = null
                                page = 1
                            }
                            style {
                                backgroundColor(Color("#e74c3c"))
                                color(Color("#ffffff"))
                                border(0.px)
                                padding(5.px, 10.px)
                                cursor("pointer")
                                fontSize(0.8.em)
                            }
                        }) { Text("X") }
                    }
                }

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
                            page = 1
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
                                DomSideEffect { it.asDynamic().selected = true }
                            }
                            Text("유저 이름")
                        }
                        Option("ID") {
                            if (searchState.searchMode == MahjongState.SearchMode.ID) {
                                DomSideEffect { it.asDynamic().selected = true }
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
                                    searchState.searchUserId = null
                                    autocompleteList = emptyList()
                                    isAutocompleteOpen = false
                                    page = 1
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
                                searchState.searchUserId = keyword.toLongOrNull()
                                page = 1
                            }
                        }
                        style {
                            backgroundColor(Color("#444"))
                            color(Color("#f1f1f1"))
                            border(1.px, LineStyle.Solid, Color("#555"))
                            padding(5.px)
                        }
                    })

                    if (searchState.searchUserId != null) {
                        Button(attrs = {
                            onClick {
                                searchState.searchUserId = null
                                searchState.searchUserName = ""
                                page = 1
                            }
                            style {
                                backgroundColor(Color("#e74c3c"))
                                color(Color("#ffffff"))
                                border(0.px)
                                padding(5.px, 10.px)
                                cursor("pointer")
                            }
                        }) { Text("초기화") }
                    }

                    // Autocomplete Dropdown
                    if (searchState.searchMode == MahjongState.SearchMode.NAME && isAutocompleteOpen) {
                        Ul(attrs = {
                            style {
                                position(Position.Absolute)
                                property("top", "100%")
                                left(100.px) // Select 박스 너비 고려
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
                                        searchState.searchUserId = user.id
                                        searchState.searchUserName = user.name
                                        isAutocompleteOpen = false
                                        page = 1
                                    }
                                    onMouseEnter {
                                        (it.target as? HTMLElement)?.style?.backgroundColor = "#444"
                                    }
                                    onMouseLeave {
                                        (it.target as? HTMLElement)?.style?.backgroundColor =
                                            if (searchState.searchUserId == user.id) "#444" else "transparent"
                                    }
                                    style {
                                        padding(8.px, 12.px)
                                        cursor("pointer")
                                        property("border-bottom", "1px solid #444")
                                        if (searchState.searchUserId == user.id) {
                                            backgroundColor(Color("#444"))
                                        }
                                    }
                                }) {
                                    Text(user.name)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            P { Text("로딩 중...") }
            return@MahjongLayout
        }
        val content = result?.content ?: emptyList()
        if (content.isEmpty()) {
            P { Text("대국 기록이 없습니다.") }
            return@MahjongLayout
        }

        for (record in content) {
            Div(attrs = {
                onClick {
                    routeState.navigate(Route.Services.MahjongRecordDetail(serverId, record.id.toString()))
                }
                style {
                    marginBottom(30.px)
                    padding(20.px)
                    backgroundColor(Color("#333"))
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, Color("#444"))
                    property("box-shadow", "0 4px 6px rgba(0,0,0,0.3)")
                    cursor("pointer")
                }
            }) {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceBetween)
                        alignItems(AlignItems.Center)
                        marginBottom(15.px)
                        paddingBottom(10.px)
                        property("border-bottom", "1px solid #555")
                        fontSize(0.9.em)
                        color(Color("#bbb"))
                    }
                }) {
                    Span { Text("ID: ${record.id}") }
                    Span { Text("기록자: ${record.createdByName} (${record.createdAt})") }
                }
                Table(attrs = {
                    style {
                        width(100.percent)
                        property("border-collapse", "collapse")
                        textAlign("center")
                    }
                }) {
                    Thead {
                        Tr {
                            listOf("위치", "유저", "점수", "포인트 변화").forEach {
                                Th(attrs = {
                                    style {
                                        padding(10.px)
                                        backgroundColor(Color("#444"))
                                        color(Color("#3498db"))
                                        fontWeight("bold")
                                        property("border", "1px solid #555")
                                    }
                                }) { Text(it) }
                            }
                        }
                    }
                    Tbody {
                        for (userInfo: MahjongRecord.UserScore in record.scoreOrders) {
                            Tr {
                                Td(attrs = {
                                    style {
                                        padding(10.px)
                                        property("border", "1px solid #555")
                                    }
                                }) { Text(userInfo.seki?.kanji?.toString() ?: "N/A") }
                                Td(attrs = {
                                    title(userInfo.userId.toString())
                                    onClick {
                                        it.stopPropagation()
                                        kotlinx.browser.window.navigator.clipboard.writeText(userInfo.userId.toString())
                                        kotlinx.browser.window.alert("ID가 복사되었습니다: ${userInfo.userId}")
                                    }
                                    style {
                                        padding(10.px)
                                        property("border", "1px solid #555")
                                        cursor("pointer")
                                    }
                                }) { Text(userInfo.userName) }
                                Td(attrs = {
                                    style {
                                        padding(10.px)
                                        property("border", "1px solid #555")
                                        fontWeight("500")
                                    }
                                }) { Text(userInfo.score.commaFormat()) }
                                Td(attrs = {
                                    style {
                                        padding(10.px)
                                        property("border", "1px solid #555")
                                        fontWeight("bold")
                                        color(
                                            when {
                                                userInfo.pointDeltaStringified.startsWith("+") -> Color("#f1948a")
                                                userInfo.pointDeltaStringified.startsWith("-") -> Color("#85c1e9")
                                                else -> Color("#f1f1f1")
                                            }
                                        )
                                    }
                                }) { Text(userInfo.pointDeltaStringified) }
                            }
                        }
                    }
                }
            }
        }

        // Simple Pagination
        val maxPage = result?.maxPage ?: 1
        var inputPage by remember(page) { mutableStateOf(page.toString()) }

        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                gap(10.px)
                marginTop(20.px)
            }
        }) {
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                    gap(20.px)
                }
            }) {
                Button(attrs = {
                    if (page <= 1) disabled()
                    onClick { if (page > 1) page-- }
                }) {
                    Text("이전")
                }
                Text("페이지 $page / $maxPage")
                Button(attrs = {
                    if (page >= maxPage) disabled()
                    onClick { if (page < maxPage) page++ }
                }) {
                    Text("다음")
                }
            }

            // 페이지 입력 이동
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(10.px)
                }
            }) {
                Input(type = InputType.Number, attrs = {
                    value(inputPage)
                    onInput { inputPage = it.value?.toString() ?: "" }
                    onKeyDown {
                        if (it.key == "Enter") {
                            val targetPage = inputPage.toIntOrNull()
                            if (targetPage != null && targetPage in 1..maxPage) {
                                page = targetPage
                            }
                        }
                    }
                    style {
                        width(50.px)
                        padding(5.px)
                        backgroundColor(Color("#444"))
                        color(Color("#f1f1f1"))
                        border(1.px, LineStyle.Solid, Color("#555"))
                        textAlign("center")
                    }
                })
                Button(attrs = {
                    onClick {
                        val targetPage = inputPage.toIntOrNull()
                        if (targetPage != null && targetPage in 1..maxPage) {
                            page = targetPage
                        } else {
                            kotlinx.browser.window.alert("1에서 $maxPage 사이의 숫자를 입력해주세요.")
                        }
                    }
                }) {
                    Text("이동")
                }
            }
        }
    }

}

private fun Int.commaFormat(): String {
    val sign = if (this < 0) "-" else ""
    val absStr = kotlin.math.abs(this).toString()

    return sign + absStr
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

