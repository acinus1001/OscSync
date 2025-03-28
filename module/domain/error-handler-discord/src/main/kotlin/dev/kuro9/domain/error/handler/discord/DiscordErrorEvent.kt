package dev.kuro9.domain.error.handler.discord

import dev.kuro9.internal.error.handler.ServerErrorEvent
import net.dv8tion.jda.api.events.GenericEvent

data class DiscordErrorEvent(
    override val t: Throwable,
    val discordEvent: GenericEvent
) : ServerErrorEvent(t)