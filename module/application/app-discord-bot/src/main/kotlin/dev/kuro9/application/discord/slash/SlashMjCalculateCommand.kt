package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.mahjong.calc.service.MjCalculateService
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

@Component
class SlashMjCalculateCommand(private val mjCalculateService: MjCalculateService) : SlashCommandComponent {
    override val commandData: SlashCommandData = Command("mjc", "마작 패 계산기") {
        subcommand("score", "부수/판수 계산.") {
            option<String>("tehai", "손패. 123m123s12333t77z 과 같은 형식으로 입력하세요.", required = true)
            option<String>("tsumo", "쯔모한 패. 1m 과 같은 형식으로 입력하세요. ron 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("ron", "론한 패. 1m 과 같은 형식으로 입력하세요. tsumo 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("huro", "론한 패. 123m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val requestTime = event.timeCreated.toInstant()
        val duration = Duration.between(requestTime, Instant.now()).toKotlinDuration()
        event.reply(duration.toString()).setEphemeral(false).await()
    }
}