package dev.kuro9.module.front.application.homepage

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.components.NavBar
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

@Composable
fun App() {
    H1 { Text("Hello World!") }
    NavBar()
}