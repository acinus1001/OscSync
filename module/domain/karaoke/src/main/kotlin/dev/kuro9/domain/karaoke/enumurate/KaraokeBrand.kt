package dev.kuro9.domain.karaoke.enumurate

enum class KaraokeBrand(val queryName: String) {
    TJ("tj"),
    KY("kumyoung");

    companion object {
        fun parse(value: String): KaraokeBrand {
            return when (value.lowercase()) {
                TJ.queryName -> TJ
                KY.queryName, "ky" -> KY
                else -> throw IllegalArgumentException("unknown karaoke brand: $value")
            }
        }
    }
}