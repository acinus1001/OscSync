package dev.kuro9.internal.google.ai.model

import com.google.genai.types.Schema

interface RequestSchema {
    val schema: Map<String, Schema>
}