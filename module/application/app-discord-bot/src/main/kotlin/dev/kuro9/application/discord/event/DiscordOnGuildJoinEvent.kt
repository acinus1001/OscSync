package dev.kuro9.application.discord.event

import dev.kuro9.domain.discord.bot.guilds.service.DiscordBotGuildService
import dev.kuro9.internal.discord.model.DiscordEventHandler
import io.github.harryjhin.slf4j.extension.info
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import org.springframework.stereotype.Component

@Component
class DiscordOnGuildJoinEvent(
    private val discordBotGuildService: DiscordBotGuildService,
) : DiscordEventHandler<GenericGuildEvent> {
    override val kClass = GenericGuildEvent::class

    override suspend fun handle(event: GenericGuildEvent) {
        when (event) {
            is GuildJoinEvent -> {
                info { "guild join: ${event.guild.name}" }
                discordBotGuildService.joinGuildUpsert(
                    botId = event.jda.selfUser.idLong,
                    guildId = event.guild.idLong,
                    guildName = event.guild.name,
                    guildIconUrl = event.guild.iconUrl
                )
            }

            is GuildLeaveEvent -> {
                info { "guild leave: ${event.guild.name}" }
                discordBotGuildService.leaveGuild(
                    botId = event.jda.selfUser.idLong,
                    guildId = event.guild.idLong
                )
            }
        }
    }
}