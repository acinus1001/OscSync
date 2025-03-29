package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.common.logger.useLogger
import dev.kuro9.internal.discord.DiscordConfigProperties
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.interactions.commands.updateCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
internal class SlashCommandListener(
    private val slashCommands: List<SlashCommandComponent>,
    private val property: DiscordConfigProperties,
) : DiscordEventHandler<SlashCommandInteractionEvent> {
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }
    private val log by useLogger()

    override val kClass = SlashCommandInteractionEvent::class

    override suspend fun handle(event: SlashCommandInteractionEvent) {
        commandMap[event.name]?.handleEvent(event) ?: errorLog("No such command: ${event.fullCommandName}")
    }

    override fun initialize(jda: JDA): Unit = jda.run {
        when (property.testGuildLong) {
            null -> updateCommands {
                addCommands(slashCommands.map { it.commandData })
            }

            else -> {
                log.info("Registering slash commands to test guild: ${property.testGuildLong}")
                getGuildById(property.testGuildLong)!!.updateCommands {
                    addCommands(slashCommands.map { it.commandData })
                }
            }
        }.queue()
    }
}