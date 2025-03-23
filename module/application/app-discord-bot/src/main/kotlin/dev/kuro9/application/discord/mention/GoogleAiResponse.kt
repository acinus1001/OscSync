package dev.kuro9.application.discord.mention

import com.google.genai.types.Schema
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAiResponse(
    val outputText: String,
    val lightControl: LightControl? = null,
) {

    @Serializable
    data class LightControl(
        val desireState: Boolean,
        val deviceName: String,
    )
}

internal fun GoogleAiResponse.Companion.getSchema(
    deviceList: List<String>
): Schema {
    val lightControlSchema = Schema.builder()
        .type("object")
        .nullable(true)
        .description("사용자가 명시적으로 기기를 켜거나 끄는 조작을 요청한다면 not-null")
        .properties(
            mapOf(
                "desireState" to Schema.builder()
                    .type("boolean")
                    .description("기기를 켜는 요청이면 true, 끄는 요청이면 false")
                    .build(),
                "deviceName" to Schema.builder()
                    .type("string")
                    .description("사용자가 조작 요청한 기기 이름 중 가장 매칭되는 이름")
                    .enum_(deviceList)
                    .build()
            )
        )
        .build()

    val rootSchema = Schema.builder()
        .type("object")
        .properties(
            mapOf(
                "outputText" to Schema.builder()
                    .type("string")
                    .description("사용자에게 출력될 응답")
                    .build(),
                "lightControl" to lightControlSchema
            )
        )
        .build()

    return rootSchema
}