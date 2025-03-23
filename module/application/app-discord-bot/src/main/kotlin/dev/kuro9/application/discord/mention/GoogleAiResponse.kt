package dev.kuro9.application.discord.mention

import com.google.genai.types.Schema
import dev.kuro9.internal.google.ai.model.ResponseSchema
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAiResponse(
    val outputText: String,
    val lightControl: LightControl? = null,
) {
    companion object : ResponseSchema {
        override val schema: Schema = Schema.builder()
            .type("object")
            .properties(
                mapOf(
                    "outputText" to Schema.builder()
                        .type("string")
                        .description("사용자에게 출력될 응답")
                        .build(),
                    "lightControl" to LightControl.schema
                )
            )
            .build()
    }

    @Serializable
    data class LightControl(
        val desireState: Boolean,
    ) {
        companion object : ResponseSchema {
            override val schema: Schema = Schema.builder()
                .type("object")
                .description("사용자가 명시적으로 조명을 켜거나 끄는 조작을 요청한다면 not-null")
                .properties(
                    mapOf(
                        "desireState" to Schema.builder()
                            .type("boolean")
                            .description("조명을 켜는 요청이면 true, 끄는 요청이면 false")
                            .build()
                    )
                )
                .build()
        }
    }
}