package dev.kuro9.module.front.application.homepage.page.services.mahjong.record

import androidx.compose.runtime.*
import dev.kuro9.module.front.application.homepage.network.MahjongApiService
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.multiplatform.common.types.app.homepage.mahjong.MahjongDetailRecord
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.koin.compose.koinInject
import kotlin.math.absoluteValue

@Composable
fun MahjongRecordDetailPage(serverId: Long, recordId: Long, routeState: RouteViewModel) =
    MahjongLayout(serverId, routeState) {
        val mahjongApiService: MahjongApiService = koinInject()
        var record by remember { mutableStateOf<MahjongDetailRecord?>(null) }
        var isLoading by remember { mutableStateOf(true) }


        LaunchedEffect(serverId, recordId) {
            isLoading = true
            try {
                record = mahjongApiService.getRecord(
                    guildId = serverId,
                    recordId = recordId,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }

        H2 { Text("대국 상세 기록 (ID : $recordId)") }
        if (isLoading) {
            P { Text("대국 ID $recordId 로딩 중...") }
            return@MahjongLayout
        }
        if (record == null) {
            window.alert("대국 ID $recordId 의 정보를 찾을 수 없습니다. 다시 시도해 주세요.")
            routeState.navigate(Route.Services.MahjongRecords(serverId))
            return@MahjongLayout
        }
        val gameInfo: MahjongDetailRecord = record!!

        Div(attrs = {
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
                Span { Text("ID: ${gameInfo.id}") }
                Span { Text("기록자: ${gameInfo.createdByName} (${gameInfo.createdAt})") }
            }

            val imageUrl = gameInfo.imageBase64Url
            if (imageUrl != null) {
                Img(src = imageUrl, attrs = {
                    style {
                        maxWidth(100.percent)
                        display(DisplayStyle.Block)
                        marginLeft(0.px)
                        marginRight(0.px)
                        property("margin-left", "auto")
                        property("margin-right", "auto")
                        marginBottom(20.px)
                        borderRadius(4.px)
                    }
                })
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
                    for (userInfo: MahjongDetailRecord.UserScore in gameInfo.scoreOrders) {
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
                                    window.navigator.clipboard.writeText(userInfo.userId.toString())
                                    window.alert("ID가 복사되었습니다: ${userInfo.userId}")
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

            Div(attrs = {
                style {
                    marginTop(20.px)
                    padding(15.px)
                    backgroundColor(Color("#3d3d3d"))
                    borderRadius(6.px)
                    fontSize(0.85.em)
                    color(Color("#ccc"))
                    property("border", "1px solid #4a4a4a")
                }
            }) {
                val setting = gameInfo.scoreSettingInfo
                Div {
                    B { Text("설정 정보") }
                }
                Div(attrs = { style { marginTop(5.px) } }) {
                    Text("시작 점수: ${setting.startScore.commaFormat()} / 반환 점수: ${setting.returnScore.commaFormat()}")
                }
                Div(attrs = { style { marginTop(5.px) } }) {
                    Text("우마: [${setting.umaFirst.explicitPlus()}, ${setting.umaSecond.explicitPlus()}, ${setting.umaThird.explicitPlus()}, ${setting.umaFourth.explicitPlus()}]")
                    if (setting.umaFirst.absoluteValue == setting.umaFourth.absoluteValue && setting.umaSecond.absoluteValue == setting.umaThird.absoluteValue)
                        Text("\t(${setting.umaFirst} - ${setting.umaSecond})")
                }
            }

            if (gameInfo.modifyLogs.isNotEmpty()) {
                Div(attrs = {
                    style {
                        marginTop(30.px)
                        padding(15.px)
                        borderRadius(8.px)
                        fontSize(0.9.em)
                        color(Color("#ecf0f1"))
                        property("border-top", "1px solid #444")
                    }
                }) {
                    H3 { Text("수정 이력") }
                    Ul {
                        gameInfo.modifyLogs.forEach { log ->
                            var isExpanded by remember { mutableStateOf(false) }

                            Li(attrs = {
                                onClick { isExpanded = !isExpanded }
                                style {
                                    marginBottom(5.px)
                                    padding(10.px)
                                    borderRadius(4.px)
//                                    listStyle("none")
                                    cursor("pointer")
                                    if (isExpanded) {
                                        backgroundColor(Color("#34495e"))
                                    }
                                }
                            }) {
                                Div(attrs = {
                                    style {
//                                        fontWeight("bold")
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        alignItems(AlignItems.Center)
                                    }
                                }) {
                                    Span {
                                        Text(
                                            when (log.type) {
                                                MahjongDetailRecord.ModifyLog.LogType.NEW -> "init: ${log.createdByName} / ${log.createdAt}"
                                                MahjongDetailRecord.ModifyLog.LogType.MODIFY -> "modify: ${log.createdByName} / ${log.createdAt}"
                                                MahjongDetailRecord.ModifyLog.LogType.DELETE -> "delete: ${log.createdByName} / ${log.createdAt}"
                                            }
                                        )
                                    }
                                    Span(attrs = {
                                        style {
                                            fontSize(0.8.em)
                                            color(Color("#95a5a6"))
                                        }
                                    }) {
                                        Text(if (isExpanded) "▲ Close" else "▼ Diff")
                                    }
                                }

                                if (isExpanded) {
                                    Div(attrs = {
                                        style {
                                            marginTop(10.px)
                                            marginLeft(10.px)
                                            fontSize(0.9.em)
                                            color(Color("#bdc3c7"))
                                            padding(10.px)
                                            property("border-left", "2px solid #7f8c8d")
                                        }
                                    }) {
                                        class ChangeLine(val text: String, val type: String)

                                        val changes = mutableListOf<ChangeLine>()

                                        fun addChange(label: String, old: Any?, new: Any?) {
                                            if (old == new) return
                                            if (old != null) {
                                                changes.add(ChangeLine("- $label: $old", "DELETE"))
                                            }
                                            if (new != null) {
                                                changes.add(ChangeLine("+ $label: $new", "ADD"))
                                            }
                                        }

                                        addChange("동가 ID", log.originalTouUserId, log.newTouUserId)
                                        addChange("동가 점수", log.originalTouUserScore, log.newTouUserScore)
                                        addChange("남가 ID", log.originalNanUserId, log.newNanUserId)
                                        addChange("남가 점수", log.originalNanUserScore, log.newNanUserScore)
                                        addChange("서가 ID", log.originalShaUserId, log.newShaUserId)
                                        addChange("서가 점수", log.originalShaUserScore, log.newShaUserScore)
                                        addChange("북가 ID", log.originalPeiUserId, log.newPeiUserId)
                                        addChange("북가 점수", log.originalPeiUserScore, log.newPeiUserScore)

                                        if (changes.isEmpty()) {
                                            Text("변경된 내용 없음")
                                        } else {
                                            changes.forEach { change ->
                                                Div(attrs = {
                                                    style {
                                                        display(DisplayStyle.Flex)
                                                        alignItems(AlignItems.Center)
                                                        padding(2.px, 5.px)
                                                        borderRadius(2.px)
                                                        marginBottom(2.px)
                                                        when (change.type) {
                                                            "ADD" -> {
                                                                backgroundColor(Color("rgba(46, 204, 113, 0.2)"))
                                                                color(Color("#2ecc71"))
                                                            }

                                                            "DELETE" -> {
                                                                backgroundColor(Color("rgba(231, 76, 60, 0.2)"))
                                                                color(Color("#e74c3c"))
                                                            }

                                                            else -> {}
                                                        }
                                                        property("font-family", "monospace")
                                                    }
                                                }) {
                                                    Text(change.text)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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

private fun Int.explicitPlus(): String = "${if (this > 0) "+" else ""}$this"
