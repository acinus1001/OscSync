package dev.kuro9.internal.google.ai.model

import com.google.genai.types.Schema
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAiRequest(
    val outputText: String,
    val lightControl: LightControl? = null,
) {
    companion object : RequestSchema {
        override val schema = mapOf(
            "outputText" to Schema.builder()
                .type("string")
                .description("사용자에게 출력될 응답")
                .build(),
            "lightControl" to Schema.builder()
                .type("object")
                .properties(LightControl.schema)
                .build()
        )
    }

    @Serializable
    data class LightControl(
        val desireState: Boolean,
    ) {
        companion object : RequestSchema {
            override val schema = mapOf(
                "desireState" to Schema.builder()
                    .type("boolean")
                    .description("조명을 켜는 요청이면 true, 끄는 요청이면 false")
                    .build()
            )
        }
    }
}