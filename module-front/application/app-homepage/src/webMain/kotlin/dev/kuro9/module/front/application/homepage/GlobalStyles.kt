package dev.kuro9.module.front.application.homepage

import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.media

object GlobalStyles : StyleSheet() {
    val mobileOnly by style {
        display(DisplayStyle.None)
        media("screen and (max-width: 768px)") {
            self style {
                display(DisplayStyle.Block)
            }
        }
    }
    val desktopOnly by style {
        display(DisplayStyle.Flex)
        media("screen and (max-width: 768px)") {
            self style {
                display(DisplayStyle.None)
            }
        }
    }
}