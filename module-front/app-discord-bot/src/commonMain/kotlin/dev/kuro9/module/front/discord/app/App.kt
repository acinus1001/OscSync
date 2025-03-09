@file:Suppress("FunctionName")

package dev.kuro9.module.front.discord.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.kuro9.module.front.discord.app.config.FragmentConfig
import dev.kuro9.module.front.discord.app.page.RootComponent
import dev.kuro9.module.front.discord.app.page.main.MainPage

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        val childStack by rootComponent.childStack.subscribeAsState()
        Children<FragmentConfig, RootComponent.ChildComponent>(
            stack = childStack,
            animation = stackAnimation(slide()),
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.ChildComponent.Main -> MainPage(instance.component)
            }
        }
    }
}