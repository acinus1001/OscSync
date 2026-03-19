package dev.kuro9.module.front.application.homepage.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun NavBar() {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            padding(16.px)
        }
    }) {
        Div { Text("kuro9") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
            }
        }) {
            MenuItem("Home")
            MenuItem("About")
            MenuItem("Contact")
        }
    }
}

@Composable
fun MenuItem(text: String) {
    A(attrs = {
        style {
            cursor("pointer")
        }

        onClick {
            println("$text 클릭")
        }
    }) { Text(text) }
}