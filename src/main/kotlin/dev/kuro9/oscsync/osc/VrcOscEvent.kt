package dev.kuro9.oscsync.osc

sealed interface VrcOscEvent<T> {
    val address: String
    val type: String
    val value: T

    data class Bool(
        override val address: String,
        override val value: Boolean,
    ) : VrcOscEvent<Boolean> {
        override val type = "Bool"
    }
}