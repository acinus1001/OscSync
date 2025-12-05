package dev.kuro9.domain.discord.logging.service

import dev.kuro9.domain.discord.logging.enums.DiscordEventType
import dev.kuro9.domain.discord.logging.repository.table.DiscordEventLogs
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import org.jetbrains.exposed.v1.jdbc.insert
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class DiscordLoggingService {

    @EventListener
    @Transactional
    fun handleSlash(event: SlashCommandInteractionEvent) {
        val author = event.user.idLong
        val guild = event.guild?.idLong
        val channel = event.channel.idLong
        val command = event.fullCommandName
        val args = event.options.joinToString("\n") { option: OptionMapping ->
            option.toString()
        }
        val requestTime = event.timeCreated.toSeoulTime()

        DiscordEventLogs.insert {
            it[this.userId] = author
            it[this.guildId] = guild
            it[this.channelId] = channel
            it[this.command] = command
            it[this.args] = args
            it[this.type] = DiscordEventType.SLASH
            it[this.requestAt] = requestTime
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    @EventListener
    @Transactional
    fun handleMessage(event: MessageReceivedEvent) {
        val author = event.author.idLong
        val guild = event.guild.idLong
        val channel = event.channel.idLong
        val command = "message"
        val args = event.message.contentRaw
        val requestTime = event.message.timeCreated.toSeoulTime()

        DiscordEventLogs.insert {
            it[this.userId] = author
            it[this.guildId] = guild
            it[this.channelId] = channel
            it[this.command] = command
            it[this.args] = args
            it[this.type] = DiscordEventType.MESSAGE
            it[this.requestAt] = requestTime
            it[this.createdAt] = LocalDateTime.now()
        }
    }

    @EventListener
    @Transactional
    fun handleMessage(event: ButtonInteractionEvent) {
        val author = event.user.idLong
        val guild = event.guild?.idLong
        val channel = event.channel.idLong
        val command = event.componentId
        val requestTime = event.timeCreated.toSeoulTime()

        DiscordEventLogs.insert {
            it[this.userId] = author
            it[this.guildId] = guild
            it[this.channelId] = channel
            it[this.command] = command
            it[this.args] = event.componentId
            it[this.type] = DiscordEventType.BUTTON
            it[this.requestAt] = requestTime
            it[this.createdAt] = LocalDateTime.now()
        }
    }


    private fun OffsetDateTime.toSeoulTime(): LocalDateTime {
        return this.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime().toKotlinLocalDateTime()
    }
}