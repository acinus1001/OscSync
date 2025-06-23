package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.enums.PaiType

class MjHead(override val paiList: List<MjPai>) : MjComponent, MjFuuProvider {
    init {
        check(paiList.size == 2)
        check(paiList.first() == paiList.last())
    }

    fun isZikazeAtama(ziKaze: MjKaze): Boolean {
        return (paiList.first() == MjPai(ziKaze.toMjPaiNotationNum(), PaiType.Z))
    }

    fun isBakazeAtama(baKaze: MjKaze): Boolean {
        return (paiList.first() == MjPai(baKaze.toMjPaiNotationNum(), PaiType.Z))
    }

    fun isSanGenAtama(): Boolean {
        return (paiList.first().num in 5..7)
    }

    override fun hasAgariHai(agariHai: MjPai): Boolean {
        return agariHai in paiList
    }

    override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
        var basicFuu = 0
        if (agariHai.pai == paiList.first()) {
            basicFuu += 2
            if (agariHai.isTsumo()) basicFuu += 2
        }

        if (isZikazeAtama(ziKaze)) basicFuu += 2
        if (isBakazeAtama(baKaze)) basicFuu += 2
        if (isSanGenAtama()) basicFuu += 2

        return basicFuu
    }

    override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int {
        var basicFuu = 0

        if (isZikazeAtama(ziKaze)) basicFuu += 2
        if (isBakazeAtama(baKaze)) basicFuu += 2
        if (isSanGenAtama()) basicFuu += 2

        return basicFuu
    }

    override fun getPaiType(): PaiType = paiList.first().type

    override fun isMenzen(): Boolean = true

    override fun isHuro(): Boolean = false

    override fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return paiList.count { it.isAkaDora } +
                doraPaiList.sumOf { doraPai -> paiList.count { doraPai == it } }
    }

    override fun containsYaoPai(): Boolean = paiList.any { it.isYao() }
    override fun isAllYaoPai(): Boolean = paiList.all { it.isYao() }
    override fun isAllNoduPai(): Boolean = paiList.all { it.isNodu() }
    override fun all(predicate: (MjPai) -> Boolean): Boolean = paiList.all(predicate)
    override fun any(predicate: (MjPai) -> Boolean): Boolean = paiList.any(predicate)

    override fun toString(): String {
        val (num, type) = paiList.first()
        return "$num$num$type"
    }
}