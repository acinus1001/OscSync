package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.util.asyncDeferReply
import dev.kuro9.domain.ai.memory.service.AiMasterMemoryService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashAiControlCommand(
    private val memoryService: AiMasterMemoryService,
) : SlashCommandComponent {

    override val commandData = Command("ai", "AI 관련 설정을 조작합니다.") {
        group("memory", "현재 사용자에 대해 채널간 공유되는 마스터 메모리 관련 메뉴") {
            subcommand("list", "현재 사용자에 대한 마스터 메모리 리스트를 출력합니다.")
            subcommand("clear", "현재 저장된 마스터 메모리를 모두 삭제합니다.")
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply = event.asyncDeferReply()

        when (event.subcommandGroup) {
            "memory" -> when (event.subcommandName) {
                "list" -> listMemory(event, deferReply)
                "clear" -> clearMemory(event, deferReply)
                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }

            else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
        }
    }

    private suspend fun listMemory(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val memoryList = withContext(Dispatchers.IO) {
            memoryService.findAllWithIndex(event.user.idLong)
        }

        Embed {
            title = "200 OK"
            description = "${event.user.asMention} 님의 전역 메모리 : ${memoryList.size}개 / 10개"

            memoryList.forEach { (index, memory) ->
                field {
                    name = "[${index}] memory"
                    inline = false
                }
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun clearMemory(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val affected = memoryService.revokeAll(event.user.idLong).await()

        Embed {
            title = "200 OK"
            description = "Cleared $affected memories."
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {
            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }
}