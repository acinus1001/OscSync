package dev.kuro9.internal.google.ai.service

import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GenerateContentResponse
import com.google.genai.types.Schema
import dev.kuro9.internal.google.ai.model.GoogleAiToken
import org.springframework.stereotype.Service

@Service
class GoogleAiService(private val token: GoogleAiToken) {

    private val client = Client.builder()
        .apiKey(token.token)
        .build()

    private val schema = Schema.builder()
        .type("object")
        .properties(
            buildMap {
                put(
                    "isLightControl",
                    Schema.builder().type("boolean").description("사용자가 명시적으로 조명을 켜거나 꺼 달라고 한다면 true").build()
                )
                put("outputText", Schema.builder().type("string").description("사용자에게 출력될 응답").build())
            }
        )
        .build()

    fun test(text: String): GenerateContentResponse {
        return client.models.generateContent(
            "gemini-2.0-flash-lite",
            text,
            GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(schema)
                .build()
        )
    }
}