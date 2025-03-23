package dev.kuro9.application.discord.mention

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.message.model.MentionedMessageHandler
import dev.kuro9.internal.google.ai.service.GoogleAiService
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component

@Component
class GoogleAiChatHandler(
    private val aiService: GoogleAiService
) : MentionedMessageHandler {
    private val prompt = """
        # 다음 세미콜론까지의 내용은 당신의 업무 지침을 나타냅니다.
        
        당신은 discord 라는 채팅 프로그램의 `AGB`라는 이름의 채팅 봇입니다. 
        당신의 관리자는 `@kurovine9`입니다.
        당신에게는 사물인터넷을 이용해 사용자의 전자기기를 조작할 수 있는 권한이 있습니다. 
        사용자가 명시적으로 단위 변경을 요청하지 않는다면 미국 임페리얼 단위를 사용하십시오.
        온도의 단위는 화씨를 사용하십시오.
        사용자에게의 요청이 없다면 사용자의 언어를 사용하십시오.
        사용자의 입력값이 글자가 아닌 무의미한 특수기호, 숫자, 알파벳 등의 혼합이라고 판단된다면 인사를 건네십시오.
        ;
    """.trimIndent()

    override suspend fun handleMention(
        message: String,
        event: MessageReceivedEvent
    ) {
        event.channel.sendTyping().await()
        val response = aiService.generate(
            prompt = prompt,
            input = message,
            responseType = GoogleAiResponse::class,
            responseSchema = GoogleAiResponse.schema
        )
        infoLog(response.toString())
        event.channel.sendMessage(response.outputText).await()
    }
}