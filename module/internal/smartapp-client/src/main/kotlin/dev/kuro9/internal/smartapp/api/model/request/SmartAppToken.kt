package dev.kuro9.internal.smartapp.api.model.request

import kotlinx.serialization.Serializable

@[JvmInline Serializable]
value class SmartAppToken private constructor(val headerValue: String) {
    companion object {
        fun of(token: String) = SmartAppToken("Bearer $token")
    }
}