package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLUListElement

@Composable
fun Services() {
    H3 { Text("다른 서비스 목록") }
    Ul {
        domain("gitlab.kuro9.dev", "private git server")
    }
}

@Composable
private fun ElementScope<HTMLUListElement>.domain(domain: String, description: String) {
    Li {
        A(attrs = { onClick { window.open("https://$domain") } }) {
            Text("$domain : $description")
        }
    }
}