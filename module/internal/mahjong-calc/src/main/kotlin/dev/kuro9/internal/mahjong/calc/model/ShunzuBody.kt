package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.model.MjBody.Shunzu

class ShunzuBody internal constructor(
    override val paiList: List<MjPai>,
    override val isHuroBody: Boolean = false,
) : Shunzu {
    fun isRyoumen(agariHai: MjPai): Boolean {
        return when (paiList.indexOf(agariHai)) {
            0 -> (paiList.last().num != 9)
            1 -> false
            2 -> (paiList.last().num != 1)
            else -> throw IllegalArgumentException("$agariHai 가 이 몸통($this)에 존재하지 않습니다.")
        }
    }

    override fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int {
        return if (isRyoumen(agariHai.pai)) 0 else 2
    }

    override fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int = 0

    override fun containsYaoPai(): Boolean = paiList.any { it.isYao() }
    override fun isAllYaoPai(): Boolean = false
    override fun isAllNoduPai(): Boolean = false

    override fun toString(): String {
        return paiList.joinToString(separator = "", postfix = paiList.first().type.toString()) { it.num.toString() }
            .let {
                if (isHuroBody) "CHI-($it)" else it
            }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> return false
            !is ShunzuBody -> return false
            else -> (other.isHuroBody == isHuroBody) and other.paiList.zip(paiList).all { (a, b) -> a == b }
        }
    }

    override fun hashCode(): Int {
        var result = paiList.hashCode()
        result = 31 * result + isHuroBody.hashCode()
        return result
    }

}