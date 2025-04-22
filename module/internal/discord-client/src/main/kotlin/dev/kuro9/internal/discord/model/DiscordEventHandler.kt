package dev.kuro9.internal.discord.model

import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import kotlin.reflect.KClass

interface DiscordEventHandler<T : GenericEvent> : CoroutineEventListener {
    val kClass: KClass<T>
    suspend fun handle(event: T)
    fun initialize(jda: JDA): Unit {}

    override suspend fun onEvent(event: GenericEvent) {
        if (kClass.isInstance(event)) {
            @Suppress("UNCHECKED_CAST")
            handle(event as T)
        }
    }
}