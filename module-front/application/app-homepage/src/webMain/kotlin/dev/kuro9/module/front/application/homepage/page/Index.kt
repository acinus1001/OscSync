package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun Index() {
    Div {
        Br()
        Text("안녕하세요! 여기는 제 웹페이지의 root 입니다."); Br()
        Br()
        Text("이 페이지는 discord 봇에서 제공하고 있던 서비스를 웹으로 옮기고, 기타 이것저것 하고 싶은 것을 하기 위해 만들었어요."); Br()
        Text("사실 그냥 제 페이지를 만들어 보고 싶었어요. 도메인을 구입한지는 3년정도 된 것 같은데, 퇴사하고 시간이 좀 비어서 이제서야 만들고 있어요."); Br()
        Br()
        Text("만약 페이지에 뭔가 보여야 할 것이 안 보인다고 생각된다면, 저에게 알려 주거나 문의 메뉴를 통해 전달해 주세요."); Br()
        Br()
        Text("좋은 하루 보내세요 :>")
    }
}