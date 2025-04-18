package dev.kuro9.internal.google.ai.service

import com.google.genai.Client
import com.google.genai.types.*
import dev.kuro9.internal.google.ai.dto.GoogleAiChatResponse
import dev.kuro9.internal.google.ai.dto.GoogleAiToken
import dev.kuro9.internal.google.ai.dto.GoogleAiToolDto
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class GoogleAiService(token: GoogleAiToken) {

    private val chatModelVersion = "gemini-2.5-flash-preview-04-17"
    private val searchModelVersion = "gemini-2.0-flash"

    private val client = Client.builder()
        .apiKey(token.token)
        .build()

    private val googleSearchTool = listOf(Tool.builder().googleSearch(GoogleSearch.builder().build()).build())

    suspend fun chat(
        systemInstruction: String,
        input: String,
        tools: List<GoogleAiToolDto> = emptyList(),
        chatLog: List<Content> = emptyList()
    ): GoogleAiChatResponse {
        val nowSessionChatLog = mutableListOf<Content>()
        nowSessionChatLog += input.toUserChatContent()

        val response = client.models.generateContent(
            chatModelVersion,
            chatLog + nowSessionChatLog,
            GenerateContentConfig.builder()
                .candidateCount(1)
                .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                .tools(tools.toTools())
                .build()
        )

        val nonWaitFuncName = tools.filter { !it.needToolResponse }.map { it.name }
        val waitFuncName = tools.filter { it.needToolResponse }.map { it.name }

        val nonWaitFunctionCall = response.functionCalls()?.filter {
            it.name().getOrNull() in nonWaitFuncName
        } ?: emptyList()

        val waitFunctionCall = response.functionCalls()?.filter {
            it.name().getOrNull() in waitFuncName
        } ?: emptyList()

        if (waitFunctionCall.isEmpty()) {
            // 응답 반환

            CoroutineScope(currentCoroutineContext()).launch {
                nonWaitFunctionCall.forEach {
                    launch {
                        val handler = tools.first { t -> it.name().getOrNull() == t.name }
                        handler.toolResponseConsumer(it.args().get())
                    }
                }
            }

            nowSessionChatLog += response.toTextResponseContent()
            return GoogleAiChatResponse(
                result = response.text()!!,
                sessionChatLog = nowSessionChatLog,
            )
        }

        val toolResponse = CoroutineScope(currentCoroutineContext()).async {
            nonWaitFunctionCall.forEach {
                launch {
                    val handler = tools.first { t -> it.name().getOrNull() == t.name }
                    handler.toolResponseConsumer(it.args().get())
                }
            }

            waitFunctionCall.map {
                async {
                    nowSessionChatLog += it.toRequestContent()
                    val handler = tools.first { t -> it.name().getOrNull() == t.name }
                    handler.name to handler.toolResponseConsumer(it.args().get())
                }
            }.awaitAll()
        }.await()

        nowSessionChatLog += toolResponse.toFunctionResponseContent()

        val responseWithTool = client.models.generateContent(
            chatModelVersion,
            chatLog + nowSessionChatLog,
            GenerateContentConfig.builder()
                .candidateCount(1)
                .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                .tools(tools.toTools())
                .build()
        )
        nowSessionChatLog += responseWithTool.toTextResponseContent()
        return GoogleAiChatResponse(
            result = responseWithTool.text()!!,
            sessionChatLog = nowSessionChatLog,
        )
    }

    suspend fun search(query: String): String {

        val systemInstruction = """
            당신은 다른 자동화된 봇을 위한 검색 결과 제공 서비스입니다. 
            최대한 짧고 간결하게 응답을 요약해 제공하십시오.
            반드시 영미 단위계(인치, 화씨, 파운드, 온스 등) 로 응답을 변환하여 제공하십시오.
        """.trimIndent()

        return client.models.generateContent(
            searchModelVersion,
            query.toUserChatContent(),
            GenerateContentConfig.builder()
                .candidateCount(1)
                .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                .tools(googleSearchTool)
                .build()
        ).text()!!
    }


    private fun String.toUserChatContent(): Content {
        return Content.builder()
            .role("user")
            .parts(listOf(Part.fromText(this)))
            .build()
    }

    private fun List<GoogleAiToolDto>.toTools(): List<Tool> {
        return listOf(Tool.builder().functionDeclarations(this.map(GoogleAiToolDto::function)).build())
    }

    private fun GenerateContentResponse.toTextResponseContent(): Content {
        return Content.builder()
            .role("model")
            .parts(listOf(Part.fromText(this.text()!!)))
            .build()
    }

    private fun FunctionCall.toRequestContent(): Content {
        return Content.builder()
            .role("model")
            .parts(
                listOf(
                    Part.builder()
                        .functionCall(this)
                        .build()
                )
            )
            .build()

    }

    private fun List<Pair<String, Map<String, Any?>>>.toFunctionResponseContent(): Content {
        return Content.builder()
            .role("user")
            .parts(
                this.map { (name, result) -> Part.fromFunctionResponse(name, result) }
            )
            .build()
    }
}