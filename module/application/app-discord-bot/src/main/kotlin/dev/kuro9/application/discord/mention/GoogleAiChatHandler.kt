package dev.kuro9.application.discord.mention

import dev.kuro9.common.logger.infoLog
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.message.model.MentionedMessageHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.google.ai.service.GoogleAiService
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import org.springframework.stereotype.Component

@Component
class GoogleAiChatHandler(
    private val aiService: GoogleAiService,
    private val smartAppUserService: SmartAppUserService,
    slashCommands: List<SlashCommandComponent>
) : MentionedMessageHandler {
    private val commandDataList = slashCommands
        .map {
            val map = it.commandData.toData().toMap().also { makeMapSmall(it) }

            val node = replaceMapToNode(map)
            node
        }
        .let(minifyJson::encodeToString)
        .also(::println)


    override suspend fun handleMention(
        message: String,
        event: MessageReceivedEvent
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            event.channel.sendTyping().await()
        }.start()
        val userDeviceNameList = smartAppUserService
            .getRegisteredDevices(event.author.idLong)
            .map { it.deviceName }
        val response = runCatching {
            aiService.generate(
                prompt = getPrompt(userDeviceNameList),
                input = message,
                responseType = GoogleAiResponse::class,
                responseSchema = GoogleAiResponse.getSchema(userDeviceNameList)
            )
        }.onFailure { ex ->

        }.getOrThrow()
        infoLog(response.toString())
        val (outputText, lightControl) = response
        CoroutineScope(Dispatchers.Default).launch {
            if (lightControl != null) {
                smartAppUserService.executeDeviceByName(
                    userId = event.author.idLong,
                    deviceName = lightControl.deviceName,
                    desireState = lightControl.desireState,
                )
            }
        }
        event.channel.sendMessage(outputText).await()
    }

    @Serializable
    private data class CommandInfo(
        val name: String,
        val description: String,
        val options: List<CommandOption>,
    )

    @Serializable
    private data class CommandOption(
        val name: String,
        val description: String,
        val type: String,
        val isMandatory: Boolean,
        val isAutoCompletable: Boolean,
    )

    private fun makeMapSmall(map: MutableMap<String, Any?>) {
        map.remove("name_localizations")
        map.remove("description_localizations")
        map.remove("type")
        map.remove("integration_types")
        map.replace(
            "contexts",
            (map.getOrElse("contexts") { emptyList<String>() } as List<String>).map {
                InteractionContextType.fromKey(it)
            })
        (map["options"] as List<MutableMap<String, Any?>>?)?.forEach { makeMapSmall(it) }
    }

    private fun replaceMapToNode(element: Any?): JsonElement {
        return when (element) {
            null -> JsonNull
            is Number -> JsonPrimitive(element.toString())
            is String -> JsonPrimitive(element.toString())
            is Boolean -> JsonPrimitive(element.toString())
            is Map<*, *> -> minifyJson.encodeToJsonElement(element.mapValues { replaceMapToNode(it.value) } as Map<String, JsonElement>)
            is Array<*> -> minifyJson.encodeToJsonElement(element.map { replaceMapToNode(it) })
            is List<*> -> minifyJson.encodeToJsonElement(element.map { replaceMapToNode(it) })
            is Enum<*> -> JsonPrimitive(element.toString())
            is JsonElement -> element
            else -> throw IllegalArgumentException("Unsupported element type ${element.javaClass.canonicalName}")
        }
    }


    private fun getPrompt(deviceNameList: List<String>): String = """
        # 다음 세미콜론까지의 내용은 당신의 업무 지침을 나타냅니다.
        
        당신은 discord 라는 채팅 프로그램의 `AGB`라는 이름의 채팅 봇입니다. 
        사무적인 대답보다는 사용자에게 친근감을 표현해 주십시오.
        당신의 관리자는 `<@!400579163959853056>`입니다. 
        당신에게는 사물인터넷을 이용해 사용자의 전자기기를 조작할 수 있는 권한이 있습니다.
        명령어 사용 또는 채팅창에서 당신을 멘션/DM해 전자기기를 조작할 수 있습니다. 
        ${
        if (deviceNameList.isEmpty()) "하지만 해당 사용자는 등록된 기기가 없습니다. lightControl 필드에 항상 null을 반환하고, 기기 등록을 유도하십시오."
        else "사용자의 기기 목록은 다음과 같습니다. 요청한 기기 이름이 다음 리스트에 없는 것 같다면 lightControl 필드에 null을 반환하고 사용자에게 기기 등록을 유도하십시오. $deviceNameList"
    }
        사용자가 명시적으로 단위 변경을 요청하지 않는다면 미국 임페리얼 단위를 사용하십시오.
        온도의 단위는 화씨를 사용하십시오.
        응답은 한국어를 사용하십시오.
        사용자의 입력값이 글자가 아닌 무의미한 특수기호, 숫자, 알파벳 등의 혼합이라고 판단된다면 인사를 건네십시오.
        명령어는 `/`를 앞에 붙여 사용하고, 하위 명령어는 스페이스로 붙여 사용합니다. 
        명령어 사용을 유도할 때는 백틱 등으로 감싸 표시해 주세요.
        사용가능한 명령어는 다음과 같습니다.
        
        ```json
        $commandDataList
        ```
        ;
    """.trimIndent()
}