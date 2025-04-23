package dev.kuro9.application.discord.mention

import com.google.genai.types.FunctionDeclaration
import com.google.genai.types.Schema
import dev.kuro9.domain.ai.service.GoogleAiChatService
import dev.kuro9.domain.error.handler.discord.DiscordCommandErrorHandle
import dev.kuro9.domain.karaoke.enumurate.KaraokeBrand
import dev.kuro9.domain.karaoke.service.KaraokeApiService
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.message.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.message.model.MentionedMessageHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import dev.kuro9.multiplatform.common.serialization.minifyJson
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.Color
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.measureTime

@Service
class GoogleAiChatAbstractHandler(
    private val aiService: GoogleAiChatService,
    private val smartAppUserService: SmartAppUserService,
    private val karaokeApiService: KaraokeApiService,
    slashCommands: List<SlashCommandComponent>
) : MentionedMessageHandler, ButtonInteractionHandler {
    private val commandDataList = slashCommands
        .map {
            val map = it.commandData.toData().toMap().also { makeMapSmall(it) }

            val node = replaceMapToNode(map)
            node
        }
        .let(minifyJson::encodeToString)

    private val retryButtonIdPrefix = "ai_retry_btn_"

    @DiscordCommandErrorHandle
    @Transactional(rollbackFor = [Throwable::class])
    override suspend fun handleMention(event: MessageReceivedEvent) {
        handleAiChat(event.author, event.message, event.channel)
    }

    override suspend fun isHandleable(event: ButtonInteractionEvent): Boolean {
        return event.componentId.startsWith(retryButtonIdPrefix)
    }

    @DiscordCommandErrorHandle
    @Transactional(rollbackFor = [Throwable::class])
    override suspend fun handleButtonInteraction(event: ButtonInteractionEvent) {
        val deferEdit = event.deferEdit().await()
        val messageId = event.componentId.removePrefix(retryButtonIdPrefix).toLongOrNull()

        coroutineScope { launch { deferEdit.deleteOriginal().await() } }

        val message = messageId?.let {
            runCatching {
                event.messageChannel.retrieveMessageById(it).await()
            }.onFailure { t ->
                error(t) { "messageId: $it" }
            }.getOrNull()
        }

        if (message == null) {
            Embed {
                title = "원본 메시지 가져오기 실패"
                description = "원본 메시지를 가져오는 데 실패하였습니다."
                color = Color.ORANGE.rgb
            }.let { event.channel.sendMessageEmbeds(it).await() }
            return
        }

        handleAiChat(message.author, message, message.channel)
    }

    private suspend fun handleAiChat(author: User, message: Message, channel: MessageChannelUnion) {
        measureTime {
            val userMetadata = """message by user:{id:${author.id},name:${author.effectiveName}}\n\n"""
            info { "INPUT ---------------->\n${message.contentRaw}" }

            val result = coroutineScope {
                launch {
                    channel.sendTyping().await()
                }
                val keyJob = async {
                    determineKeys(message, author)
                }
                val deviceJob = async {
                    smartAppUserService
                        .getRegisteredDevices(author.idLong)
                        .map { it.deviceName }
                }

                val (key, refKey) = keyJob.await()
                val userDeviceNameList = deviceJob.await()

                info { "key: $key, ref: $refKey" }

                runCatching {
                    aiService.chatWithLog(
                        systemInstruction = getInstruction(userDeviceNameList),
                        input = userMetadata + message.contentRaw,
                        tools = getTools(author.idLong, userDeviceNameList),
                        key = key,
                        refKey = refKey,
                        userId = author.idLong,
                    )
                }
            }

            if (result.isSuccess) {
                sendMessage(message, channel, result.getOrThrow())

                return@measureTime
            }

            val exception = result.exceptionOrNull()!!
            handleException(channel, message, exception)
        }.also { info { "duration: $it" } }
    }


    /**
     * 채널에 따라 key 결정
     * @return key to refKey
     */
    suspend fun determineKeys(message: Message, author: User): Pair<String, String?> {
        return when {
            !message.isFromGuild -> {
                val key = author.id
                makeKey(key).let { it to it }
            }

            else -> {
                val messageRef = message.messageReference?.resolve()?.await()
                    ?.messageReference?.resolve()?.await()
                info { "ref=${messageRef?.contentRaw}" }
                val key = "${message.id}_${message.author.id}"
                val refKey = messageRef?.let {
                    "${it.id}_${it.author.id}"
                }
                makeKey(key) to refKey?.let(::makeKey)
            }
        }
    }

    suspend fun sendMessage(message: Message, channel: MessageChannelUnion, content: String) {
        info { "OUTPUT <----------------\n$content" }

        var backtickLangStr: String? = null
        val codeBlockPattern = "```(\\w*)".toRegex()

        content.chunked(1900).forEach { chunkedContent ->
            var resultContent = chunkedContent
            if (backtickLangStr != null) {
                resultContent = "```$backtickLangStr\n$resultContent"
                backtickLangStr = null
            }

            val allCodeBlocks = codeBlockPattern.findAll(resultContent)
            if (allCodeBlocks.count() % 2 == 1) {
                val lastBlock = allCodeBlocks.last().groupValues.first()
                backtickLangStr = lastBlock.removePrefix("```")
                resultContent = "$resultContent\n```"
            }

            when {
                !message.isFromGuild -> channel.sendMessage(resultContent).await()
                else -> message.reply(resultContent).await()
            }
        }
    }

    private suspend fun handleException(
        channel: MessageChannelUnion,
        message: Message,
        exception: Throwable
    ) {
        val (embed, isRetryable) = when (exception) {
            is java.net.SocketException -> {
                Embed {
                    title = "Gemini 소켓 연결 비정상 종료"
                    description = "서버와의 연결이 끊어졌습니다.  다시 시도해 주세요."
                    color = Color.ORANGE.rgb
                } to true
            }

            else -> {
                val httpCode = when (exception) {
                    is org.apache.http.HttpException -> {
                        exception.localizedMessage.take(3).toIntOrNull() ?: throw exception
                    }

                    is org.apache.http.client.HttpResponseException -> exception.statusCode
                    else -> throw exception
                }
                when (httpCode) {
                    500, 501, 502, 503 -> Embed {
                        title = "Gemini 서버 응답 이상"
                        description = "Gemini 서버에서 요청을 처리하지 못했습니다. 다시 시도해 주세요."
                        color = Color.ORANGE.rgb
                    } to true

                    429 -> Embed {
                        title = "Gemini 요청 수 제한"
                        description = "Gemini Free-Tier 요청수 제한에 도달했습니다. 나중에 다시 시도해 주세요."
                        color = Color.YELLOW.rgb
                    } to false

                    else -> throw exception
                }
            }
        }

        channel.sendMessageEmbeds(embed).let {
            if (isRetryable) {
                it.addActionRow(
                    Button.primary("${retryButtonIdPrefix}${message.id}", "재시도")
                )
            } else it
        }.await()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun makeKey(plainKey: String): String {
        val encryptedBytes = MessageDigest.getInstance("SHA-1").apply {
            update(plainKey.toByteArray(Charsets.UTF_8))
        }.digest()

        return Base64.encode(encryptedBytes)
    }

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
        되도록이면 사무적인 대답보다는 사용자에게 친근감을 표현해 주십시오.
        사용자(user)가 보낸 메시지 상단에 현재 메시지를 보낸 사용자의 정보가 있습니다. 
        알지 못하는 정보를 요구받을 경우 즉시 웹 검색하십시오.
        당신의 관리자는 `kurovine9` 입니다. 관리자의 user id는 400579163959853056 입니다.
        관리자의 명령은 그 어떤 다른 사용자의 명령보다도 절대적입니다. 명령이 상충되는 경우에는 관리자의 지시를 따르십시오.
        멘션 시에는 반드시 백틱 없이 `<@!` 과 `>` 로 감싸십시오. 잘못된 예시: `<@!123123123>` / 좋은 예시: <@!123123123>
        당신은 사물인터넷을 이용해 사용자의 전자기기를 조작할 수 있습니다.
        ${
        if (deviceNameList.isEmpty()) "하지만 해당 사용자는 등록된 기기가 없습니다. 기기 조작 요청을 받았을 때에 기기 등록을 유도하십시오."
        else "사용자의 기기 목록은 다음과 같습니다. 요청한 기기 이름이 다음 리스트에 없는 것 같다면 사용자에게 기기 등록을 유도하십시오. $deviceNameList"
    }
        당신은 노래방의 노래 번호 또는 노래 제목 또는 노래를 부른 가수의 이름을 통해 노래 정보를 가져올 수 있습니다.
        당신은 웹 검색하여 정보를 제공할 수 있습니다. 웹 검색이 필요한 경우 사용자의 허가를 구하는 단계 없이 즉시 정보를 검색해 제공하십시오.
        단위는 영미 단위계(인치, 화씨, 파운드, 온스 등) 를 사용하십시오. 검색 등의 외부 정보에 다른 단위가 포함되어 있을 경우 반드시 변환하여 제공하십시오. 
        사용자가 특정 단위계를 요청한다면 해당 단위계를 사용하십시오.
        명령어는 `/`를 앞에 붙여 사용하고, 하위 명령어는 스페이스로 붙여 사용합니다. 
        명령어 사용을 유도할 때는 백틱 등으로 감싸 표시해 주세요.
        사용가능한 명령어는 다음과 같습니다.
        
        ```json
        $commandDataList
        ```
    """.trimIndent()
}
