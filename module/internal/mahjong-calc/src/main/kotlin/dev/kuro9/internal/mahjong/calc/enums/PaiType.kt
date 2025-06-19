package dev.kuro9.internal.mahjong.calc.enums

/**
 * - M: 만수
 * - P: 통수
 * - S: 삭수
 * - Z: 자패
 */
enum class PaiType {
    M, P, S, Z;

    companion object {
        fun of(char: Char): PaiType {
            return PaiType.valueOf(char.uppercase())
        }

        fun Char.isPaiType() = uppercase() in PaiType.entries.map(Enum<*>::name)
    }
}