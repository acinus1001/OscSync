package dev.kuro9.module.front.discord.app.config

import kotlinx.serialization.Serializable

@Serializable
sealed class FragmentConfig {

    @Serializable
    data object Main : FragmentConfig()
}