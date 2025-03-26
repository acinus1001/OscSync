package dev.kuro9.internal.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.updateCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
internal class SlashCommandListener(
    private val slashCommands: List<SlashCommandComponent>
) : DiscordEventHandler<SlashCommandInteractionEvent> {
    private val commandMap: Map<String, SlashCommandComponent> = slashCommands.associateBy { it.commandData.name }

    override val kClass = SlashCommandInteractionEvent::class

    override suspend fun handle(event: SlashCommandInteractionEvent) {
        commandMap[event.name]?.handleEvent(event) ?: errorLog("No such command: ${event.fullCommandName}")
    }

    override fun initialize(jda: JDA): Unit = jda.run {
        CoroutineScope(Dispatchers.IO).launch {
            val job1 = launch {
                getGuildById(588993828309041153L)?.updateCommands {
                    addCommands(slashCommands.map { it.commandData })
                }?.await()
            }
            val job2 = launch {
                updateCommands {
                    addCommands(slashCommands.map { it.commandData })
                }.await()
            }

            job1.join()
            job2.join()
            infoLog("Command Initialize Complete")
        }
    }
}