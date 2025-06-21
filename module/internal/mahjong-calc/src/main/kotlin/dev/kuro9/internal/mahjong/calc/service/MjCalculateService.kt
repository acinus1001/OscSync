package dev.kuro9.internal.mahjong.calc.service

import dev.kuro9.internal.mahjong.calc.enums.PaiType
import dev.kuro9.internal.mahjong.calc.enums.PaiType.Companion.isPaiType
import dev.kuro9.internal.mahjong.calc.model.*
import org.springframework.stereotype.Service

@Service
class MjCalculateService {

    fun parseTeHai(
        teHaiStr: String,
        agariHaiStr: String,
        isRon: Boolean,
        huroBody: Array<String> = emptyArray(),
        anKanBody: Array<String> = emptyArray(),
    ): MjTeHai? {
        val parsedHuro = huroBody.map { MjBody.of(parse(it), isHuro = true) }.toTypedArray()
        val parsedAnKang: Array<MjBody> = anKanBody.map { MjBody.of(parse(it), isHuro = false) }.toTypedArray()

        require(parsedAnKang.all { it is KanBody }) {
            "입력된 값이 깡이 아닙니다."
        }

        val outerBody = (parsedHuro + parsedAnKang)

        val teHai = MjTeHai.parse(parse(teHaiStr), MjAgariHai.of(parse(agariHaiStr).single(), isRon), *outerBody)

        val result = teHai.maxByOrNull { it.getTopFuuHan() }

        return result
    }

    fun parseTeHai(teHai: List<MjPai>, agariHai: MjAgariHai, vararg huroBody: MjBody): MjTeHai {
        return MjTeHai.parse(teHai, agariHai, *huroBody).maxBy { it.getTopFuuHan() }
    }

    private fun parse(str: String): List<MjPai> {
        var typePtr: PaiType = PaiType.of(str.last())
        val resultList = mutableListOf<MjPai>()

        for (char in str.replace(" ", "").uppercase().reversed().drop(1)) {
            when {
                char.isDigit() -> resultList.add(MjPai.of(char.digitToInt(), typePtr))
                char.isPaiType() -> typePtr = PaiType.of(char)
                else -> throw IllegalArgumentException("$char is not a valid pai")
            }
        }

        return resultList
    }
}