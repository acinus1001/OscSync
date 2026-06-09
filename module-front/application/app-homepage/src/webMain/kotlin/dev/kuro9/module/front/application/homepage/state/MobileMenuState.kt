package dev.kuro9.module.front.application.homepage.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object MobileMenuState {
    var customMenu: (@Composable () -> Unit)? by mutableStateOf(null)

    fun setMenu(menu: (@Composable () -> Unit)?) {
        customMenu = menu
    }
}
