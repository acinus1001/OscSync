package dev.kuro9.module.front.discord.app.util

actual fun goExternalPage(url: String) {
    kotlinx.browser.window.location.href = url
}