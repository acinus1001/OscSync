package dev.kuro9.module.front.application.homepage.page.services.mahjong.stats

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MahjongStatsPage(serverId: Long, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("통계 ($serverId)") }
        P { Text("서버 통계가 여기에 표시됩니다.") }
    }
}

@Composable
fun MahjongUserStatsPage(serverId: Long, userId: Long, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("사용자 통계 ($serverId - $userId)") }
        P { Text("사용자 $userId 에 대한 상세 통계가 여기에 표시됩니다.") }
    }
}
