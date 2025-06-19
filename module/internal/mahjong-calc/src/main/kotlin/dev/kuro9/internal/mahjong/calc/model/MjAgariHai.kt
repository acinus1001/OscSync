package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.PaiType

sealed interface MjAgariHai : MjComponent {
    val pai: MjPai

    companion object {
        fun ron(pai: MjPai): MjAgariHai = Ron(pai)
        fun tsumo(pai: MjPai): MjAgariHai = Tsumo(pai)
        fun of(pai: MjPai, isRon: Boolean): MjAgariHai = when (isRon) {
            true -> Ron(pai)
            false -> Tsumo(pai)
        }
    }

    fun isRon(): Boolean = this is Ron
    fun isTsumo(): Boolean = this is Tsumo

    override fun getPaiType(): PaiType = pai.type
    override fun isMenzen() = true
    override fun isHuro() = false
    override fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return doraPaiList.count { it == pai } + if (pai.isAkaDora) 1 else 0
    }

    override fun containsYaoPai(): Boolean = pai.isYao()
    override fun isAllYaoPai(): Boolean = pai.isYao()
    override fun isAllNoduPai(): Boolean = pai.isNodu()
    override fun all(predicate: (MjPai) -> Boolean): Boolean = predicate(pai)
    override fun any(predicate: (MjPai) -> Boolean): Boolean = predicate(pai)

    data class Ron(override val pai: MjPai) : MjAgariHai {
        override fun toString() = "RON($pai)"
    }

    data class Tsumo(override val pai: MjPai) : MjAgariHai {
        override fun toString() = "TSUMO($pai)"
    }
}