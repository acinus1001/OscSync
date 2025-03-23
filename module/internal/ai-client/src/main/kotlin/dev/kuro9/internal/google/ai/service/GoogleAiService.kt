package dev.kuro9.internal.google.ai.service

import com.google.genai.Client
import com.google.genai.types.*
import dev.kuro9.internal.google.ai.model.GoogleAiToken
import dev.kuro9.multiplatform.common.serialization.minifyJson
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class GoogleAiService(private val token: GoogleAiToken) {

    private val client = Client.builder()
        .apiKey(token.token)
        .build()

    private val searchTool = Tool.builder()
        .googleSearch(GoogleSearch.builder().build()).build()
//        .googleSearchRetrieval(GoogleSearchRetrieval.builder().build()).build()

    @OptIn(InternalSerializationApi::class)
    fun <T : Any> generate(
        prompt: String,
        input: String,
        responseType: KClass<T>,
        responseSchema: Schema,
    ): T {
        val response = client.models.generateContent(
            "gemini-2.0-flash",
            input,
            GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(responseSchema)
                .systemInstruction(
                    Content.builder()
                        .parts(
                            listOf(
                                Part.builder()
                                    .text(prompt)
                                    .build()
                            )
                        ).build()
                )
//                .tools(listOf(searchTool))
                .build()
        ).text()!!

        return minifyJson.decodeFromString<T>(responseType.serializer(), response)
    }
}