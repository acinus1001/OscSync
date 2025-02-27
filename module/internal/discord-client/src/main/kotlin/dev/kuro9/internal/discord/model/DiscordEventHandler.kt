package dev.kuro9.internal.discord.model

import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass

interface DiscordEventHandler<T : GenericEvent> {
    val kClass: KClass<T>
    fun handle(event: T)
}