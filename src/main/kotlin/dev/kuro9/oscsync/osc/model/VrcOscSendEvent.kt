package dev.kuro9.oscsync.osc.model

sealed interface VrcOscSendEvent<out T : Any> {
    val address: String
    val payload: T

    data class BoolType(
        override val address: String,
        override val payload: Boolean
    ) : VrcOscSendEvent<Boolean>

    data class IntType(
        override val address: String,
        override val payload: Int
    ) : VrcOscSendEvent<Int>

    data class FloatType(
        override val address: String,
        override val payload: Float
    ) : VrcOscSendEvent<Float>


    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> of(
            address: String,
            payload: T
        ) : VrcOscSendEvent<T> {
            return when (payload) {
                is Boolean -> BoolType(address, payload)
                is Int -> IntType(address, payload)
                is Float -> FloatType(address, payload)

                else -> throw NotImplementedError("")
            } as VrcOscSendEvent<T>
        }
    }
}