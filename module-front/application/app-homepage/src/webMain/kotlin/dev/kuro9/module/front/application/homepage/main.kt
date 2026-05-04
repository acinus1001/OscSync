package dev.kuro9.module.front.application.homepage

import org.jetbrains.compose.web.renderComposable
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin { modules(module) }
    renderComposable("root") {
        App()
    }
}