package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze

interface MjFuuProvider {
    fun hasAgariHai(agariHai: MjPai): Boolean

    /**
     *  해당 머리/몸통에 오름패가 있는 상황에서의 그 블럭에 대한 부수를 리턴합니다.
     *  @return 블럭 자체에 대한 부수 + 쯔모부수 + 화료형태 부수
     */
    fun getAgariBlockFuu(agariHai: MjAgariHai, ziKaze: MjKaze, baKaze: MjKaze): Int

    /**
     * 해당 머리/몸통에 오름패가 없는 상황에서의 그 블럭에 대한 부수를 리턴합니다.
     * @return 블럭 자체에 대한 부수
     */
    fun getBlockFuu(ziKaze: MjKaze, baKaze: MjKaze): Int
}