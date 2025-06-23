package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.PaiType


sealed interface MjBody : MjComponent, MjFuuProvider {
    override val paiList: List<MjPai>
    val isHuroBody: Boolean

    override fun getPaiType(): PaiType = paiList.first().type
    override fun isMenzen(): Boolean = !isHuroBody
    override fun isHuro(): Boolean = isHuroBody
    override fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return paiList.count { it.isAkaDora } +
                doraPaiList.sumOf { doraPai ->
                    paiList.count { doraPai == it }
                }
    }

    override fun hasAgariHai(agariHai: MjPai): Boolean {
        return agariHai in paiList
    }

    override fun all(predicate: (MjPai) -> Boolean): Boolean {
        return paiList.all(predicate)
    }

    override fun any(predicate: (MjPai) -> Boolean): Boolean {
        return paiList.any(predicate)
    }

    companion object {
        fun of(paiList: List<MjPai>, isHuro: Boolean = false): MjBody {
            val paiListSorted = paiList.sorted()
            val (firstElementNum, firstElementType) = paiListSorted.first()
            return when {
                paiList.size !in 3..4 -> throw IllegalArgumentException("몸통은 3개 또는 4개의 패로만 구성될 수 있습니다. ")
                paiList.any { firstElementType != it.type } -> throw IllegalArgumentException("몸통은 서로 다른 종류로 구성될 수 없습니다. ")

                // 슌쯔
                paiList.any { firstElementNum != it.num } -> {
                    check(paiList.size == 3) { "슌쯔는 3개의 패로만 구성될 수 있습니다." }
                    check(
                        paiListSorted.reversed().withIndex().all { (index, pai) ->
                            firstElementNum + 2 == index + pai.num
                        }
                    ) { "슌쯔는 패가 순서대로 존재해야 합니다." }

                    ShunzuBody(paiList, isHuro)
                }

                paiList.size == 3 -> PongBody(paiList, isHuro)
                paiList.size == 4 -> KanBody(paiList, isHuro)

                else -> throw IllegalStateException("Unhandled case. 패 파싱 실패.")
            }
        }
    }

    sealed interface Shunzu : MjBody
    sealed interface Kutsu : MjBody
}