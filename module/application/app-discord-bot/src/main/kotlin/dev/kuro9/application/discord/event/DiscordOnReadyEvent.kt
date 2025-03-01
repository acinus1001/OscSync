package dev.kuro9.application.discord.event

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.springframework.stereotype.Component

@Component
class DiscordOnReadyEvent : DiscordEventHandler<ReadyEvent> {
    override val kClass = ReadyEvent::class

    override suspend fun handle(event: ReadyEvent) {
        infoLog("discord client ready")
    }
}