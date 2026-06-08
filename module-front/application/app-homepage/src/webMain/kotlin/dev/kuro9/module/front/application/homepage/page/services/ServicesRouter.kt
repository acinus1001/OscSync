package dev.kuro9.module.front.application.homepage.page.services

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.services.iot.IotRoot
import dev.kuro9.module.front.application.homepage.page.services.mahjong.*
import dev.kuro9.module.front.application.homepage.state.route.Route
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel

@Composable
fun ServicesRouter(routeState: RouteViewModel) {
    val nowPage = routeState.nowPage
    when (nowPage) {
        Route.Services.ROOT -> ServicesRoot(routeState)
        Route.Services.IOT -> IotRoot()
        Route.Services.MAHJONG -> MahjongServerSelectPage(routeState)
        is Route.Services.MahjongServer -> MahjongRecordsPage(nowPage.serverId, routeState)
        is Route.Services.MahjongRecords -> MahjongRecordsPage(nowPage.serverId, routeState)
        is Route.Services.MahjongStats -> MahjongStatsPage(nowPage.serverId, routeState)
        is Route.Services.MahjongRanks -> MahjongRanksPage(nowPage.serverId, routeState)
        is Route.Services.MahjongRecordDetail -> MahjongRecordDetailPage(nowPage.serverId, nowPage.recordId, routeState)
        is Route.Services.MahjongUserStats -> MahjongUserStatsPage(nowPage.serverId, nowPage.userId, routeState)
        else -> return
    }
}