package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*

@Composable
fun Contact() {
    Div {
        Text("연락 가능 수단")
        Ul {
            Li { Text("TEL : +82 10-1234-5678") }
            Li {
                A(attrs = { onClick { window.open("mailto:admin@kuro9.dev") } }) {
                    Text("EMAIL : admin@kuro9.dev")
                }
            }
        }
    }
}