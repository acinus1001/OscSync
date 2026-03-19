package dev.kuro9.module.front.application.homepage

import org.jetbrains.compose.web.renderComposable

fun main() {
//    startKoin { modules(appModule) }
    renderComposable("root") {
        App()
    }
}