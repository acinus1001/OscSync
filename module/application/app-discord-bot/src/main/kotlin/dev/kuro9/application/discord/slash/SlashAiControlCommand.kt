package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class SlashAiControlCommand : SlashCommandComponent {

    override val commandData = Command("ai", "AI 관련 설정을 조작합니다.") {
        group("memory", "현재 사용자에 대해 채널간 공유되는 마스터 메모리 관련 메뉴") {
            subcommand("list", "현재 사용자에 대한 마스터 메모리 리스트를 출력합니다.")
            subcommand("add", "수동으로 마스터 메모리를 추가합니다.") {
                option<String>("memory", "앞으로 적용될 새 메모리", required = true, autocomplete = false)
            }
            subcommand("delete", "수동으로 마스터 메모리를 삭제합니다.") {
                option<Int>("index", "삭제할 메모리 인덱스 번호", required = true, autocomplete = false)
            }
            subcommand("clear", "현재 저장된 마스터 메모리를 모두 삭제합니다.")
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }
}