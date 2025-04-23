package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import org.springframework.stereotype.Component

@Component
class SlashGithubCommand : SlashCommandComponent {
    override val commandData = slash("github", "return github url")

    // todo 버튼식으로 바꿀까?
    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        event.reply("https://github.com/acinus1001/OscSync").await()
    }
}