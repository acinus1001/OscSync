package dev.kuro9.internal.mahjong.calc.utils

import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.enums.MjYaku
import dev.kuro9.internal.mahjong.calc.enums.PaiType
import dev.kuro9.internal.mahjong.calc.model.*

object MjYakuParser {

    /**
     * 우연역이나 장 상황에 따른 역이 아닌 패를 보고 알 수 있는 역 목록을 반환합니다.
     */
    fun getYaku(
        agariHai: MjAgariHai,
        agariBlock: MjComponent,
        baKaze: MjKaze,
        ziKaze: MjKaze,
        vararg blocks: MjComponent
    ): Set<MjYaku> {
        val componentList = (blocks.toList() + agariBlock)
        val ziKazeHai = MjPai(ziKaze.toMjPaiNotationNum(), PaiType.Z)
        val baKazeHai = MjPai(baKaze.toMjPaiNotationNum(), PaiType.Z)
        val hakuHai = MjPai(5, PaiType.Z)
        val hatsuHai = MjPai(6, PaiType.Z)
        val chuuHai = MjPai(7, PaiType.Z)
        val yakuHai = listOf(
            ziKazeHai,
            baKazeHai,
            hakuHai,
            hatsuHai,
            chuuHai
        )

        fun MjYaku.isYakuFulFilled(): Boolean {
            if (this.onlyMenzen and (componentList.all { it.isMenzen() }.not())) return false // 멘젠한정 공통체크

            return when (this) {
                MjYaku.TSUMO -> agariHai.isTsumo()
                MjYaku.YAKU_BAKASE -> componentList.any {
                    val kutsuBody = (it as? MjBody)?.takeUnless { it is ShunzuBody } ?: return@any false

                    kutsuBody.paiList.first() == baKazeHai
                }

                MjYaku.YAKU_ZIKASE -> componentList.any {
                    val kutsuBody = (it as? MjBody)?.takeUnless { it is ShunzuBody } ?: return@any false

                    kutsuBody.paiList.first() == ziKazeHai
                }

                MjYaku.YAKU_HAKU -> componentList.any {
                    val kutsuBody = (it as? MjBody)?.takeUnless { it is ShunzuBody } ?: return@any false

                    kutsuBody.paiList.first() == hakuHai
                }

                MjYaku.YAKU_HATSU -> componentList.any {
                    val kutsuBody = (it as? MjBody)?.takeUnless { it is ShunzuBody } ?: return@any false

                    kutsuBody.paiList.first() == hatsuHai
                }

                MjYaku.YAKU_CHUU -> componentList.any {
                    val kutsuBody = (it as? MjBody)?.takeUnless { it is ShunzuBody } ?: return@any false

                    kutsuBody.paiList.first() == chuuHai
                }

                MjYaku.TANYAO -> componentList.all { it.containsYaoPai().not() }
                MjYaku.PINFU -> {
                    val head: MjHead =
                        componentList.filterIsInstance<MjHead>().takeIf { it.size == 1 }?.first() ?: return false
                    val bodyList = componentList.filterIsInstance<MjBody>().takeIf { it.isNotEmpty() } ?: return false

                    when {
                        agariBlock is MjHead -> false // 머리단기로 화료
                        bodyList.any { it !is ShunzuBody } -> false // 슌쯔가 아닌 것이 있는지 체크
                        head.paiList.first() in yakuHai -> false // 머리가 역패인지 체크
                        (agariBlock as ShunzuBody).isRyoumen(agariHai.pai).not() -> false // 양면대기 체크

                        else -> true
                    }
                }

                MjYaku.IPECO -> {
                    val shunzuBodyList =
                        componentList.filterIsInstance<ShunzuBody>().takeIf { it.size >= 2 } ?: return false
                    shunzuBodyList.size != shunzuBodyList.distinct().size
                }

                MjYaku.CHANTA -> componentList.all { it.containsYaoPai() } && componentList.any { it.getPaiType() == PaiType.Z }
                MjYaku.HONROUTOU -> componentList.all { it.isAllYaoPai() } && componentList.any { it.getPaiType() == PaiType.Z }
                MjYaku.SANSHOKU_DOUJUU -> run {
                    val shunzuBody =
                        componentList.filterIsInstance<MjBody.Shunzu>().takeIf { it.size >= 3 } ?: return@run false
                    val shanshokuBody = shunzuBody.groupBy { it.paiList.map { pai -> pai.num }.toSet() }
                        .values
                        .find { value -> value.size >= 3 } ?: return@run false

                    shanshokuBody.map { it.getPaiType() }.toSet() == setOf(PaiType.M, PaiType.P, PaiType.S)
                }

                MjYaku.SANSHOKU_DOUKOU -> run {
                    val kutsuBody =
                        componentList.filterIsInstance<MjBody.Kutsu>().takeIf { it.size >= 3 } ?: return@run false
                    val shanshokuBody = kutsuBody.groupBy { it.paiList.first().num }
                        .values
                        .find { value -> value.size >= 3 } ?: return@run false

                    shanshokuBody.map { it.getPaiType() }.toSet() == setOf(PaiType.M, PaiType.P, PaiType.S)
                }

                MjYaku.ITTKITSUKAN -> run {
                    val shunzuBodys = componentList.filterIsInstance<ShunzuBody>()
                    val (color, colorBodyList) = shunzuBodys.groupBy { it.getPaiType() }
                        .filter { (key, value) -> value.size >= 3 }
                        .entries
                        .firstOrNull() ?: return@run false

                    val checkNum: List<MjPai>.(Set<Int>) -> Boolean = {
                        this.map { pai -> pai.num }.toSet() == it
                    }

                    colorBodyList.any { it.paiList.checkNum(setOf(1, 2, 3)) }
                            && colorBodyList.any { it.paiList.checkNum(setOf(4, 5, 6)) }
                            && colorBodyList.any { it.paiList.checkNum(setOf(7, 8, 9)) }
                }

                MjYaku.TOITOI -> componentList.filterIsInstance<MjBody.Kutsu>().size == 4

                MjYaku.SANANKOU -> run {
                    val menzenKutsu = componentList.filterIsInstance<MjBody.Kutsu>().filter {
                        it.isMenzen()
                    }

                    return when {
                        menzenKutsu.size < 3 -> false // 멘젠커쯔 3개 미만이면 false
                        menzenKutsu.size == 4 -> true // 멘젠커쯔 4개면 true
                        agariBlock in menzenKutsu -> agariHai.isTsumo() // 멘젠커쯔 3개이고 화료한 블럭이 그 중 하나일 경우 쯔모일때만 true
                        else -> true // 다른 블록으로 화료한 경우 true
                    }
                }

                MjYaku.SANKANTSU -> componentList.filterIsInstance<KanBody>().size == 3
                MjYaku.CHITOITSU -> componentList.filterIsInstance<MjHead>().size == 7
                MjYaku.SHOUSANGEN -> {
                    val sangenPaiList = listOf(hakuHai, hatsuHai, chuuHai)
                    val bodys = componentList.filter { it.all { pai -> pai in sangenPaiList } }

                    bodys.size == 3 && bodys.count { it is MjHead } == 1 && bodys.count { it is MjBody } == 2
                }

                MjYaku.JUNCHANTA -> componentList.all { it.containsYaoPai() && it.getPaiType() != PaiType.Z }
                MjYaku.HONITSU -> {
                    val paiTypes = componentList.map { it.getPaiType() }.distinct()
                    PaiType.Z in paiTypes && paiTypes.size == 2
                }

                MjYaku.RYANPEKO -> {
                    val shunzuBodyList =
                        componentList.filterIsInstance<ShunzuBody>().takeIf { it.size == 4 } ?: return false
                    val bodyCount = shunzuBodyList.groupingBy { it }.eachCount()
                    1 !in bodyCount.values
                }

                MjYaku.CHINITSU -> componentList.map { it.getPaiType() }.distinct().size == 1

                MjYaku.RIICHI, MjYaku.IPPATSU, MjYaku.CHANKAN, MjYaku.HAITEI, MjYaku.HOUTEI, MjYaku.DOUBLE_RIICHI -> false
                MjYaku.TENHOU -> false
                MjYaku.CHIHOU -> false
                MjYaku.SUANKOU -> {
                    agariHai.isTsumo() &&
                            componentList.filterIsInstance<MjBody>()
                                .takeIf { it.isNotEmpty() }
                                ?.all { it !is ShunzuBody && it.isMenzen() } ?: false
                }

//                MjYaku.KOKUSHI -> TODO()
                MjYaku.DAISANGEN -> {

                    val kutsuPaiList: Set<MjPai> = componentList
                        .filterIsInstance<MjBody.Kutsu>()
                        .map { it.paiList.first() }.toSet()

                    hakuHai in kutsuPaiList && hatsuHai in kutsuPaiList && chuuHai in kutsuPaiList
                }

                MjYaku.TSUISO -> {
                    val paiTypes = componentList.map { it.getPaiType() }.distinct()
                    PaiType.Z in paiTypes && paiTypes.size == 1
                }

                MjYaku.RYUISO -> {
                    componentList.all {
                        it.all { pai ->
                            pai in listOf(
                                hatsuHai,
                                MjPai.of(2, PaiType.S),
                                MjPai.of(3, PaiType.S),
                                MjPai.of(4, PaiType.S),
                                MjPai.of(6, PaiType.S),
                                MjPai.of(8, PaiType.S),
                            )
                        }
                    }
                }

                MjYaku.CHINROTO -> {
                    componentList.all { it.isAllNoduPai() }
                }

                MjYaku.SYOUSUSI -> {
                    val kazeComponentList =
                        componentList.filter { it.all { pai -> pai.type == PaiType.Z && pai.num in 1..4 } }

                    kazeComponentList.size == 4
                            && kazeComponentList.filterIsInstance<MjBody.Kutsu>().size == 3
                            && kazeComponentList.filterIsInstance<MjHead>().size == 1
                            && kazeComponentList.map { it.paiList.first().num }.containsAll(listOf(1, 2, 3, 4))
                }

                MjYaku.CHUREN -> run {
                    val paiList = componentList.flatMap { it.paiList }
                    if (paiList.groupBy { it.type }.size >= 2) return@run false

                    val numMap = paiList.groupingBy { it.num }.eachCount()

                    numMap.getOrDefault(1, 0) >= 3
                            && numMap.getOrDefault(9, 0) >= 3
                            && (2..8).all { numMap.getOrDefault(it, 0) >= 1 }
                }
//                MjYaku.SUKANTSU -> TODO()
//                MjYaku.DAISUSI -> TODO()
                MjYaku.SUANKOU_TANKI -> {
                    agariBlock is MjHead &&
                            componentList.filterIsInstance<MjBody>()
                                .takeIf { it.isNotEmpty() }
                                ?.all { it !is ShunzuBody && it.isMenzen() } ?: false
                }

//                MjYaku.KOKUSHI_13MEN -> TODO()
                MjYaku.CHUREN_9MEN -> run {
                    val paiList = componentList.flatMap { it.paiList }
                    if (paiList.groupBy { it.type }.size >= 2) return@run false

                    val numMap = paiList.groupingBy { it.num }.eachCount()
                        .toMutableMap()
                        .apply {
                            this.computeIfPresent(agariHai.pai.num) { _, v -> v - 1 }
                        }
                        .toMap()

                    numMap[1] == 3 && numMap[9] == 3 && (2..8).all { numMap[it] == 1 }
                }

                else -> false
            }
        }

        return MjYaku.entries.filter { it.isYakuFulFilled() }
            .deleteIllegalStateYaku()
    }

    /**
     * 같이 존재할 수 없는 역을 삭제합니다.
     */
    private fun Collection<MjYaku>.deleteIllegalStateYaku(): Set<MjYaku> {
        val mutableResultSet = this.toMutableSet()

        // TODO 역만 추가
        if (MjYaku.CHINITSU in mutableResultSet) mutableResultSet.remove(MjYaku.HONITSU)
        if (MjYaku.RYANPEKO in mutableResultSet) mutableResultSet.remove(MjYaku.IPECO)
        if (MjYaku.JUNCHANTA in mutableResultSet) mutableResultSet.remove(MjYaku.CHANTA)

        return mutableResultSet
    }
}