package dev.kuro9.oscsync.osc

sealed interface VrcOscEvent<T> {
    val address: String
    val type: String
    val value: T

    data class BoolType(
        override val address: String,
        override val value: Boolean,
    ) : VrcOscEvent<Boolean> {
        override val type = "bool"
    }

    data class IntType(
        override val address: String,
        override val value: Int,
    ) : VrcOscEvent<Int> {
        override val type = "Int"
    }

    data class FloatType(
        override val address: String,
        override val value: Float,
    ) : VrcOscEvent<Float> {
        override val type = "Float"
    }

    data class UnknownType(
        override val address: String,
        override val value: String,
    ) : VrcOscEvent<String> {
        override val type = "Unknown"
    }
}