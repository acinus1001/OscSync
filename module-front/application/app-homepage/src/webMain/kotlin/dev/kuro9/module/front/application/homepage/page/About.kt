package dev.kuro9.module.front.application.homepage.page

import androidx.compose.runtime.Composable
import dev.kuro9.module.front.application.homepage.utils.PrivateImg
import dev.kuro9.module.front.application.homepage.utils.PrivateText
import kotlinx.browser.window
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*

@Composable
fun About() {
    Div {
        Text("Amō Kotlin!"); Br();
        Br()
        Text("FE : Kotlin Compose Multiplatform (Kotlin/JS)"); Br()
        Text("BE : Kotlin + Spring Boot 3"); Br()
        Br()
        Text("Source Code At : ")
        A(attrs = { onClick { window.open("https://github.com/acinus1001/OscSync") } }) { Text("https://github.com/acinus1001/OscSync") }; Br()
    }

    Hr()

    Div {
        PrivateImg(id = "64994140-ad7d-43ff-a8c7-67a04100619c") {
            style {
                width(50.percent)
                height(auto)
            }
        }
        Br()
        PrivateText("53262c60-7d65-42d6-a348-939e0235b103")
    }

}
