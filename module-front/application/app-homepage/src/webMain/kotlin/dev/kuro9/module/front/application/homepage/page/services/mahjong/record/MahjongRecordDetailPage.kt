package dev.kuro9.module.front.application.homepage.page.services.mahjong.record

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MahjongRecordDetailPage(serverId: Long, recordId: String, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("대국 상세 기록 ($serverId - $recordId)") }
        P { Text("대국 ID $recordId 에 대한 상세 정보가 여기에 표시됩니다.") }
    }
}
