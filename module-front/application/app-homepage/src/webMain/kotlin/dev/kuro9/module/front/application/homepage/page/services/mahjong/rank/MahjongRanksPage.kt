package dev.kuro9.module.front.application.homepage.page.services.mahjong.rank

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.page.services.mahjong.MahjongLayout
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MahjongRanksPage(serverId: Long, routeState: RouteViewModel) {
    MahjongLayout(serverId, routeState) {
        H2 { Text("랭킹 ($serverId)") }
        P { Text("유저 랭킹이 여기에 표시됩니다.") }
    }
}
