package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.enums.MjSeki


data class MjGameInfoVo(
    val gameSeq: MjGameSeq,
    val honba: Int = 0,
    val firstOya: MjSeki,
    val tenhouCount: Int = 0,
    val dora: List<MjPai>,
    val uraDora: List<MjPai>
) {
    /** 자풍패 */
    val zikaze: MjKaze by lazy {
        when (val seq = (-firstOya.ordinal - (gameSeq.num - 1) + 12) % 4) {
            0 -> MjKaze.TOU
            1 -> MjKaze.NAN
            2 -> MjKaze.SHA
            3 -> MjKaze.PEI

            else -> throw IllegalStateException("$seq cannot be reach")
        }
    }

    /** 장풍패 */
    val bakaze: MjKaze = gameSeq.kaze

    val isOya: Boolean = zikaze == MjKaze.TOU

    companion object {
        val Default = MjGameInfoVo(
            gameSeq = MjGameSeq(MjKaze.TOU, 1),
            firstOya = MjSeki.TOI,
            dora = emptyList(),
            uraDora = emptyList(),
        )

        fun of(
            zikaze: MjKaze,
            bakaze: MjKaze,
            honba: Int = 0,
            tenhouCount: Int = 0,
            dora: List<MjPai> = emptyList(),
            uraDora: List<MjPai> = emptyList(),
        ): MjGameInfoVo {
            val num = (5 - zikaze.toMjPaiNotationNum()) % 4 + 1
            println("num: $num")
            return MjGameInfoVo(
                firstOya = MjSeki.ZIKA,
                gameSeq = MjGameSeq(bakaze, num = num),
                honba = honba,
                tenhouCount = tenhouCount,
                dora = dora,
                uraDora = uraDora,
            )
        }
    }
}
