package dev.kuro9.module.front.application.homepage.state.user

sealed class UserEffect {
    data class OpenOAuth(val url: String) : UserEffect()
    data object Logout : UserEffect()
}