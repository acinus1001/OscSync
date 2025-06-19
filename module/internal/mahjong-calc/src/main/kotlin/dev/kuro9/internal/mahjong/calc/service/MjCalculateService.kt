package dev.kuro9.internal.mahjong.calc.service

import dev.kuro9.internal.mahjong.calc.enums.PaiType
import dev.kuro9.internal.mahjong.calc.enums.PaiType.Companion.isPaiType
import dev.kuro9.internal.mahjong.calc.model.MjAgariHai
import dev.kuro9.internal.mahjong.calc.model.MjBody
import dev.kuro9.internal.mahjong.calc.model.MjPai
import dev.kuro9.internal.mahjong.calc.model.MjTeHai
import org.springframework.stereotype.Service

@Service
class MjCalculateService {

    fun parseTeHai(teHaiStr: String, agariHaiStr: String, isRon: Boolean, vararg huroBody: String): MjTeHai {
        val parsedHuro = huroBody.map { MjBody.of(parse(it), isHuro = true) }.toTypedArray()

        val teHai = MjTeHai.parse(parse(teHaiStr), MjAgariHai.of(parse(agariHaiStr).single(), isRon), *parsedHuro)

        val result = teHai.maxBy { it.getTopFuuHan() }

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

fun main() {
    val result = MjCalculateService().parseTeHai("2234455m234p234s", "6m", true)
    println(result)
    println(result.getTopFuuHan())
}