package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.model.MjBody.Kutsu

class KanBody internal constructor(
    override val paiList: List<MjPai>,
    override val isHuroBody: Boolean = false,
) : Kutsu {
    override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
        var basic = 8
        if (isAllYaoPai()) basic *= 2

        if (agariHai.pai != paiList.first()) return if (isHuro()) basic else basic * 2

        if (isMenzen() && agariHai.isTsumo()) basic *= 2

        return basic
    }

    override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int {
        var basic = 8
        if (isAllYaoPai()) basic *= 2
        if (isMenzen()) basic *= 2
        return basic
    }

    override fun containsYaoPai(): Boolean = paiList.first().isYao()
    override fun isAllYaoPai(): Boolean = containsYaoPai()
    override fun isAllNoduPai(): Boolean = paiList.first().isNodu()

    override fun toString(): String {
        return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
            .let {
                if (isHuroBody) "KANG($it)" else it
            }
    }

    override fun equals(other: Any?): Boolean {
        when (other) {
            null -> return false
            (other !is KanBody) -> return false
        }

        other as KanBody
        return (other.isHuroBody == isHuroBody) and (other.paiList.first() == paiList.first())
    }

    override fun hashCode(): Int {
        var result = paiList.hashCode()
        result = 31 * result + isHuroBody.hashCode()
        return result
    }

}