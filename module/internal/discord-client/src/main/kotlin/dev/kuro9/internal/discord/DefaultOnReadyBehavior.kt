package dev.kuro9.internal.discord

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@[Component ConditionalOnMissingBean]
class DefaultOnReadyBehavior : DiscordEventHandler<ReadyEvent> {
    override val kClass = ReadyEvent::class

    override fun handle(event: ReadyEvent) {
        infoLog("discord client ready")
    }

}