package dev.kuro9.internal.osc.model

sealed interface VrcOscReceiveEvent<T> {
    val address: String
    val type: String
    val value: T

    data class BoolType(
        override val address: String,
        override val value: Boolean,
    ) : VrcOscReceiveEvent<Boolean> {
        override val type = "bool"
    }

    data class IntType(
        override val address: String,
        override val value: Int,
    ) : VrcOscReceiveEvent<Int> {
        override val type = "Int"
    }

    data class FloatType(
        override val address: String,
        override val value: Float,
    ) : VrcOscReceiveEvent<Float> {
        override val type = "Float"
    }

    data class UnknownType(
        override val address: String,
        override val value: String,
    ) : VrcOscReceiveEvent<String> {
        override val type = "Unknown"
    }
}