package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.util.asyncDeferReply
import dev.kuro9.domain.ai.memory.service.AiMasterMemoryService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.option
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
            subcommand("delete", "지정한 마스터 메모리를 삭제합니다.") {
                option<Long>("memory-index", "지정할 마스터 메모리 인덱스", required = true)
            }
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply = event.asyncDeferReply()

        runCatching {
            when (event.subcommandGroup) {
                "memory" -> when (event.subcommandName) {
                    "list" -> listMemory(event, deferReply)
                    "clear" -> clearMemory(event, deferReply)
                    "delete" -> deleteMemory(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t ->
            deferReply.await().editOriginalEmbeds(getDefaultExceptionEmbed(t)).await()
            return
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
                    name = "[${index}] $memory"
                    inline = false
                }
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun clearMemory(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val affected: Int = memoryService.revokeAll(event.user.idLong).await()

        Embed {
            title = "200 OK"
            description = "${affected}개 메모리 삭제 완료"
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun deleteMemory(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val memoryIndex = event.getOption("memory-index")!!.asLong
        val memory = withContext(Dispatchers.IO) {
            memoryService.findByIndex(event.user.idLong, memoryIndex)?.let {
                memoryService.revoke(event.user.idLong, memoryIndex)
            }
        } ?: throw IllegalArgumentException("memory not found")

        Embed {
            title = "200 OK"
            description = "다음 메모리 삭제 완료 : `$memory`"
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {
            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            is IllegalArgumentException -> Embed {
                title = "Index Not Found"
                description = "입력 파라미터가 정확한지 확인하십시오. 해당 입력값은 `/ai memory list`의 대괄호 내 값입니다."
                color = Color.ORANGE.rgb
            }

            else -> throw t
        }
}