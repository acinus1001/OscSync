package dev.kuro9.module.front.application.homepage.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CopyableText(textToCopy: String) {
    Span(attrs = {
        // 1. 마우스 커서를 포인터(손가락) 모양으로 바꾸어 클릭 가능하다는 시각적 힌트 제공
        style { cursor("pointer") }

        // 2. 클릭 이벤트 발생 시 클립보드에 복사
        onClick {
            window.navigator.clipboard.writeText(textToCopy)
                .then {
                    // 복사 성공 시 로직 (원하는 대로 변경 가능)
                    window.alert("복사되었습니다: $textToCopy")
                }
                .catch { error ->
                    // 복사 실패 시 로직
                    println("복사 실패: $error")
                }
        }
    }) {
        Text(textToCopy)
    }
}