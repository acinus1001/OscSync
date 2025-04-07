package dev.kuro9.internal.google.ai.dto

import com.google.genai.types.FunctionDeclaration

class GoogleAiToolDto(
    val name: String,
    val function: FunctionDeclaration,
    val needToolResponse: Boolean,
    val toolResponseConsumer: suspend (arg: Map<String, Any?>) -> Map<String, Any?>
)