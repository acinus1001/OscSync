package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.PaiType

sealed interface MjComponent {
    /** 만, 통, 삭, 자패 구별 */
    fun getPaiType(): PaiType

    /** 해당 몸통 멘젠 여부 */
    fun isMenzen(): Boolean

    /** 해당 몸통 후로 여부 */
    fun isHuro(): Boolean

    /** 도라 카운트 */
    fun getDoraCount(doraPaiList: List<MjPai>): Int

    /** 요구패 포함 여부(any) */
    fun containsYaoPai(): Boolean

    /** 모든 패 요구패(all) */
    fun isAllYaoPai(): Boolean

    /** 모든 패 노두패(all) */
    fun isAllNoduPai(): Boolean

    /** 모든 패가 해당 조건을 만족하면 true를 리턴합니다. */
    fun all(predicate: (MjPai) -> Boolean): Boolean

    /** 어떤 패가 해당 조건을 만족하면 true를 리턴합니다. */
    fun any(predicate: (MjPai) -> Boolean): Boolean
}