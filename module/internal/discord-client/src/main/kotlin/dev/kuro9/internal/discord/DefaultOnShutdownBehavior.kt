package dev.kuro9.internal.discord

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import net.dv8tion.jda.api.events.session.ShutdownEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@[Component ConditionalOnMissingBean]
class DefaultOnShutdownBehavior : DiscordEventHandler<ShutdownEvent> {
    override val kClass = ShutdownEvent::class

    override fun handle(event: ShutdownEvent) {
        infoLog("discord client shutdown")
        //TODO
    }
}