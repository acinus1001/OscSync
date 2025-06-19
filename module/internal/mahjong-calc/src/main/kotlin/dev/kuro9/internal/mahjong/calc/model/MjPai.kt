package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.PaiType

data class MjPai internal constructor(
    val num: Int,
    val type: PaiType,
    val isAkaDora: Boolean = false,
) : Comparable<MjPai> {

    /**
     * 야오패인지 여부 리턴
     */
    fun isYao(): Boolean {
        if (type == PaiType.Z) return true
        return num == 1 || num == 9
    }

    /**
     * 노두패인지 여부 리턴
     */
    fun isNodu(): Boolean {
        if (type == PaiType.Z) return false
        return num == 1 || num == 9
    }

    /**
     * 숫자패인지 여부 리턴
     */
    fun isSuziPai(): Boolean {
        return type != PaiType.Z
    }

    /**
     * 자패인지 여부 리턴
     */
    fun isZiPai(): Boolean {
        return type == PaiType.Z
    }

    companion object {
        fun of(num: Int, type: PaiType): MjPai {
            when (type) {
                PaiType.M, PaiType.P, PaiType.S -> check(num in 0..9)
                PaiType.Z -> check(num in 1..7)
            }

            return if (num == 0) MjPai(5, type, true)
            else MjPai(num, type, false)
        }
    }

    override fun compareTo(other: MjPai): Int {
        check(type == other.type) { "type not matches" }
        return num.compareTo(other.num)
    }

    override fun toString(): String {
        return if (isAkaDora) "0$type" else "$num$type"
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is MjPai -> false
            else -> num == other.num && type == other.type
        }
    }

    override fun hashCode(): Int {
        var result = num
        result = 31 * result + type.hashCode()
        result = 31 * result + isAkaDora.hashCode()
        return result
    }
}