package dev.kuro9.internal.mahjong.calc.enums

import java.util.*

enum class MjKaze {
    TOU, NAN, SHA, PEI;

    fun next(): MjKaze {
        return when (this) {
            TOU -> NAN
            NAN -> SHA
            SHA -> PEI
            PEI -> throw IllegalStateException("북 다음의 장은 없습니다.")
        }
    }

    fun prev(): MjKaze {
        return when (this) {
            TOU -> throw IllegalStateException("북 다음의 장은 없습니다.")
            NAN -> TOU
            SHA -> NAN
            PEI -> SHA
        }
    }

    fun toLocaleString(locale: Locale): String {
        return when (locale) {
            Locale.KOREAN, Locale.KOREA -> when (this) {
                TOU -> "동"
                NAN -> "남"
                SHA -> "서"
                PEI -> "북"
            }

            else -> throw NotImplementedError("$locale 은 지원되지 않습니다.")
        }
    }

    fun toMjPaiNotationNum(): Int = when (this) {
        TOU -> 1
        NAN -> 2
        SHA -> 3
        PEI -> 4
    }
}