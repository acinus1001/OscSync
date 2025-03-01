package dev.kuro9.internal.discord.model

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass

interface DiscordEventHandler<T : GenericEvent> {
    val kClass: KClass<T>
    suspend fun handle(event: T)
    fun initialize(jda: JDA): Unit {}
}