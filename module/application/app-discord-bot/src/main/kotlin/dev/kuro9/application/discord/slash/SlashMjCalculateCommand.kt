package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.mahjong.calc.service.MjCalculateService
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.awt.Color

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
        runCatching {
            when (event.subcommandName) {
                "score" -> calculateScore(event)

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t ->
            event.replyEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    private suspend fun calculateScore(event: SlashCommandInteractionEvent) {
        val deferReply = event.asyncDeferReply()

        val tehai = event.getOption("tehai")!!.asString
        val tsumo = event.getOption("tsumo")?.asString
        val ron = event.getOption("ron")?.asString
        val huro = event.getOption("huro")?.asString

        // validate input
        if (!((tsumo != null) xor (ron != null))) {
            throw IllegalArgumentException("쯔모와 론 옵션 중 하나의 옵션에만 입력하십시오.")
        }

        val huroBody = huro?.removeSurrounding(" ")?.split(" ")?.toTypedArray()

        val parsedTeHai = mjCalculateService.parseTeHai(
            teHaiStr = tehai,
            agariHaiStr = ron ?: tsumo!!,
            isRon = ron != null,
            huroBody = huroBody ?: emptyArray()
        )

        val score = parsedTeHai.getTopFuuHan()
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {

            is IllegalArgumentException -> Embed {
                title = "Invalid Input"
                description = "잘못된 입력입니다. ${t.message ?: ""}"
                color = Color.RED.rgb
            }

            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }

    private suspend fun SlashCommandInteractionEvent.asyncDeferReply(isEphemeral: Boolean = false): Deferred<InteractionHook> {
        return coroutineScope {
            async { deferReply(isEphemeral).await() }
        }
    }
}