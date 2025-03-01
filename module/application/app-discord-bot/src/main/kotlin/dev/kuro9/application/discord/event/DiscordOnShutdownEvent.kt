package dev.kuro9.application.discord.event

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.session.ShutdownEvent
import org.springframework.stereotype.Component

@Component
class DiscordOnShutdownEvent : DiscordEventHandler<ShutdownEvent> {
    override val kClass = ShutdownEvent::class

    override suspend fun handle(event: ShutdownEvent) {
        infoLog("discord client shutdown")
    }
}