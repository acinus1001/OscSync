package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import java.util.*

data class MjGameSeq(
    val kaze: MjKaze,
    val num: Int,
) {
    init {
        check(num in 1..4)
    }

    fun next(): MjGameSeq {
        if (kaze == MjKaze.PEI && num == 4) throw IllegalStateException("북4국을 초과할 수 없습니다.")

        return if (num < 4) MjGameSeq(kaze, num + 1)
        else MjGameSeq(kaze.next(), 1)
    }

    fun toLocaleString(locale: Locale): String {
        val kyokuStr = when (locale) {
            Locale.KOREA, Locale.KOREAN -> "국"
            else -> "국"
        }
        return "${kaze.toLocaleString(locale)} $num$kyokuStr"
    }

    override fun toString(): String {
        return toLocaleString(Locale.KOREAN)
    }
}
