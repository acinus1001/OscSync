package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun About() {
    Div {
        Text("Amō Kotlin!")
        Br(); Br();
        Text("Source Code At : ")
        A(attrs = { onClick { window.open("https://github.com/acinus1001/OscSync") } }) { Text("https://github.com/acinus1001/OscSync") }
    }

}
