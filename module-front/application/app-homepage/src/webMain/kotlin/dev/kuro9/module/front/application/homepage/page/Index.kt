package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun Index() {
    Div {
        Text("안녕하세요! 여기는 제 웹페이지의 root 입니다.")
    }
}