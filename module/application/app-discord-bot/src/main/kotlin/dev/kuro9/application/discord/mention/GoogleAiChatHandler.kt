package dev.kuro9.application.discord.mention

import com.google.genai.types.Content
import com.google.genai.types.FunctionDeclaration
import com.google.genai.types.Schema
import dev.kuro9.domain.error.handler.discord.DiscordCommandErrorHandle
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.service.KaraokeApiService
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.message.model.MentionedMessageHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import dev.kuro9.internal.google.ai.service.GoogleAiService
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.minn.jda.ktx.coroutines.await
import io.github.harryjhin.slf4j.extension.info
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
    private val karaokeApiService: KaraokeApiService,
    slashCommands: List<SlashCommandComponent>
) : MentionedMessageHandler {
    private val commandDataList = slashCommands
        .map {
            val map = it.commandData.toData().toMap().also { makeMapSmall(it) }

            val node = replaceMapToNode(map)
            node
        }
        .let(minifyJson::encodeToString)

    // todo db 또는 캐시화
    private val chatLogMap: MutableMap<Long, MutableList<Content>> = mutableMapOf()

    @DiscordCommandErrorHandle
    override suspend fun handleMention(
        event: MessageReceivedEvent,
        message: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            event.channel.sendTyping().await()
        }.start()

        val userChatLog = chatLogMap.getOrPut(event.author.idLong) { mutableListOf() }

        val userDeviceNameList = smartAppUserService
            .getRegisteredDevices(event.author.idLong)
            .map { it.deviceName }

        val response = aiService.chat(
            systemInstruction = getInstruction(userDeviceNameList),
            input = message,
            chatLog = userChatLog,
            tools = getTools(event.author.idLong, userDeviceNameList)
        )

        info { response }
        for (log in userChatLog) {
            info { log.toString() }
        }

        if (userChatLog.size > 100) chatLogMap.remove(event.author.idLong)

        event.channel.sendMessage(response).await()
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

    @Suppress("UNCHECKED_CAST")
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

    @Suppress("UNCHECKED_CAST")
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

    private fun getTools(
        userId: Long,
        deviceList: List<String>,
    ): List<GoogleAiToolDto> {
        return listOf(
            GoogleAiToolDto(
                name = "smartApp",
                function = FunctionDeclaration.builder()
                    .name("smartApp")
                    .description("사용자의 전자기기를 조작할 수 있는 함수")
                    .parameters(
                        Schema.builder()
                            .type("object")
                            .properties(
                                mapOf(
                                    "desireState" to Schema.builder()
                                        .type("boolean")
                                        .description("기기를 켜는 요청이면 true, 끄는 요청이면 false")
                                        .nullable(false)
                                        .build(),
                                    "deviceName" to Schema.builder()
                                        .type("string")
                                        .description("사용자가 조작 요청한 기기 이름 중 가장 유사한 이름.")
                                        .enum_(deviceList)
                                        .nullable(false)
                                        .build()
                                )
                            )
                            .build()
                    )
                    .build(),
                needToolResponse = true,
                toolResponseConsumer = {
                    println(it)
                    val deviceName: String by it
                    val desireState: Boolean by it

                    smartAppUserService.executeDeviceByName(
                        userId = userId,
                        deviceName = deviceName,
                        desireState = desireState,
                    )

                    mapOf("result" to "true")
                }
            ),
            GoogleAiToolDto(
                name = "karaokeSearch",
                function = FunctionDeclaration.builder()
                    .name("karaokeSearch")
                    .description("노래방의 노래를 검색할 수 있는 함수")
                    .parameters(
                        Schema.builder()
                            .type("object")
                            .properties(
                                mapOf(
                                    "type" to Schema.builder()
                                        .type("string")
                                        .description("검색의 종류 enum")
                                        .enum_(listOf("번호", "제목", "가수"))
                                        .nullable(false)
                                        .build(),
                                    "value" to Schema.builder()
                                        .type("string")
                                        .description("검색의 종류에 따른 검색값")
                                        .nullable(false)
                                        .build()
                                )
                            )
                            .build()
                    )
                    .build(),
                needToolResponse = true,
                toolResponseConsumer = {
                    info { it.toString() }
                    val type: String by it
                    val value: String by it

                    when (type) {
                        "번호" -> {
                            val result = karaokeApiService.getSongInfoByNo(KaraokeBrand.TJ, value.toInt())

                            buildMap {
                                put("isResultExist", result != null)
                                put("result", result?.let {
                                    mapOf(
                                        "songNo" to result.songNo.toString(),
                                        "title" to result.title,
                                        "singer" to result.singer
                                    )
                                })
                            }
                        }

                        "제목" -> {
                            val result = karaokeApiService.getSongInfoByName(KaraokeBrand.TJ, value.toString())

                            buildMap {
                                put("resultSize", result.size)
                                put("result", result.map {
                                    mapOf(
                                        "songNo" to it.songNo.toString(),
                                        "title" to it.title,
                                        "singer" to it.singer
                                    )
                                })
                            }
                        }

                        "가수" -> {
                            val result = karaokeApiService.getSongInfoByArtist(KaraokeBrand.TJ, value.toString())

                            buildMap {
                                put("resultSize", result.size)
                                put("result", result.map {
                                    mapOf(
                                        "songNo" to it.songNo.toString(),
                                        "title" to it.title,
                                        "singer" to it.singer
                                    )
                                })
                            }
                        }

                        else -> throw IllegalArgumentException("unknown type: $type")
                    }
                }
            ),
            GoogleAiToolDto(
                name = "webSearch",
                function = FunctionDeclaration.builder()
                    .name("webSearch")
                    .description("인터넷 검색 및 요약 함수")
                    .parameters(
                        Schema.builder()
                            .type("object")
                            .properties(
                                mapOf(
                                    "query" to Schema.builder()
                                        .type("string")
                                        .description("검색어")
                                        .nullable(false)
                                        .build(),
                                )
                            )
                            .build()
                    )
                    .build(),
                needToolResponse = true,
                toolResponseConsumer = {
                    val query: String by it
                    info { "args : $it" }

                    mapOf("result" to aiService.search(query))
                }
            ),
        )
    }


    private fun getInstruction(deviceNameList: List<String>): String = """
        당신은 `KGB`라는 이름의 채팅 봇입니다. (stands for : kurovine9's general bot)
        사무적인 대답보다는 사용자에게 친근감을 표현해 주십시오.
        답변은 반드시 2000자 미만으로 작성하십시오.
        알지 못하는 정보를 요구받을 경우 지체 없이 바로 웹 검색하십시오.
        당신의 관리자는 `<@!400579163959853056>`입니다. 
        당신에게는 사물인터넷을 이용해 사용자의 전자기기를 조작할 수 있는 권한이 있습니다.
        명령어 사용 또는 채팅창에서 당신을 멘션/DM해 전자기기를 조작할 수 있습니다. 
        ${
        if (deviceNameList.isEmpty()) "하지만 해당 사용자는 등록된 기기가 없습니다. 사용자에게 기기 등록을 유도하십시오."
        else "사용자의 기기 목록은 다음과 같습니다. 요청한 기기 이름이 다음 리스트에 없는 것 같다면 사용자에게 기기 등록을 유도하십시오. $deviceNameList"
    }
        당신은 노래방의 노래 번호 또는 노래 제목 또는 노래를 부른 가수의 이름을 통해 노래 정보를 가져올 수 있습니다. 사용자가 요청할 경우 정보를 제공하는 것은 당신의 의무입니다.
        사용자가 명시적으로 단위 변경을 요청하지 않는다면 미국 임페리얼 단위와 화씨를 사용하십시오.
        명령어는 `/`를 앞에 붙여 사용하고, 하위 명령어는 스페이스로 붙여 사용합니다. 
        명령어 사용을 유도할 때는 백틱 등으로 감싸 표시해 주세요.
        사용가능한 명령어는 다음과 같습니다.
        
        ```json
        $commandDataList
        ```
    """.trimIndent()
}