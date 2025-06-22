package dev.kuro9.internal.mahjong.calc.model

import dev.kuro9.internal.mahjong.calc.enums.MjYaku
import dev.kuro9.internal.mahjong.calc.enums.PaiType
import dev.kuro9.internal.mahjong.calc.utils.*

data class MjTeHai(
    val head: MjHead,
    val body: List<MjBody>,
    val agariHai: MjAgariHai,
) {
    init {
        check(body.size == 4)
    }

    override fun toString(): String {
        return "$head " + body.joinToString(" ") + " $agariHai"
    }

    val isHuro: Boolean by lazy { body.any { it.isHuro() } }
    val isMenzen: Boolean = !isHuro
    fun getDoraCount(doraPaiList: List<MjPai>): Int {
        return head.getDoraCount(doraPaiList) +
                body.sumOf { it.getDoraCount(doraPaiList) } +
                agariHai.getDoraCount(doraPaiList)
    }

    fun getYaku(gameInfo: MjGameInfoVo, isRiichi: Boolean): List<Set<MjYaku>> {
        val blockList = (body + head)
        val hasAgariHaiBlockIndex = blockList.withIndex().filter { (index, block) ->
            block.hasAgariHai(agariHai.pai)
        }.map { it.index }

        return hasAgariHaiBlockIndex.map {
            val notAgariBlockList = blockList.filterIndexed { index, _ -> index != it }
            val yakuSet = MjYakuParser.getYaku(
                agariHai,
                blockList[it],
                gameInfo.bakaze,
                gameInfo.zikaze,
                *notAgariBlockList.toTypedArray()
            )

            when (isRiichi) {
                true -> yakuSet + MjYaku.RIICHI
                false -> yakuSet
            }
        }

    }

    /**
     * 가능한 부/판수의 형태를 모두 리턴합니다.
     */
    fun getPossibleFuuHan(
        gameInfo: MjGameInfoVo,
        isRiichi: Boolean = false,
        isIppatsu: Boolean = false,
        isChankan: Boolean = false,
        isHaiTei: Boolean = false,
        isHoutei: Boolean = false,
        isDoubleRiichi: Boolean = false,
    ): Set<MjScoreVo<out MjScoreI>> {
        val blockList = (body + head)
        val hasAgariHaiBlockIndex = blockList.withIndex().filter { (index, block) ->
            block.hasAgariHai(agariHai.pai)
        }.map { it.index }

        return hasAgariHaiBlockIndex.map {
            val notAgariBlockList = blockList.filterIndexed { index, _ -> index != it }

            val yakuSet = MjYakuParser.getYaku(
                agariHai,
                blockList[it],
                gameInfo.bakaze,
                gameInfo.zikaze,
                *notAgariBlockList.toTypedArray()
            ).run {
                val yakuToAdd = mutableSetOf<MjYaku>()
                if (isRiichi) yakuToAdd += MjYaku.RIICHI
                if (isIppatsu) yakuToAdd += MjYaku.IPPATSU
                if (isChankan) yakuToAdd += MjYaku.CHANKAN
                if (isHaiTei) yakuToAdd += MjYaku.HAITEI
                if (isHoutei) yakuToAdd += MjYaku.HOUTEI
                if (isDoubleRiichi) yakuToAdd += MjYaku.DOUBLE_RIICHI

                this + yakuToAdd
            }

            val fuu = when {
                MjYaku.CHITOITSU in yakuSet -> 25
                MjYaku.PINFU in yakuSet -> when (agariHai) {
                    is MjAgariHai.Ron -> 30
                    is MjAgariHai.Tsumo -> 20
                }

                else -> {
                    val blockFuu =
                        blockList[it].getAgariBlockFuu(agariHai, gameInfo.zikaze, gameInfo.bakaze) + notAgariBlockList
                            .sumOf { block ->
                                block.getBlockFuu(
                                    gameInfo.zikaze,
                                    gameInfo.bakaze
                                )
                            } + if (this.isMenzen && this.agariHai.isRon()) 30 else 20

                    blockFuu / 10 * 10 + if (blockFuu % 10 > 0) 10 else 0
                }
            }.takeUnless { finalFuu -> finalFuu == 20 && agariHai.isRon() } ?: 30 // 쿠이핑후 형태 핸들링

            val fuuToHanVo = MjFuuToHanVo(
                fuu,
                yakuSet.sumOf { if (it.kuiSagari && !this.isMenzen) it.han - 1 else it.han },
                yakuSet
            )
            when (this.agariHai) {
                is MjAgariHai.Ron -> MjScoreUtil.getRonScore(fuuToHanVo, isOya = gameInfo.isOya)
                is MjAgariHai.Tsumo -> MjScoreUtil.getTsumoScore(fuuToHanVo)
            }
        }.toSet()
    }

    fun getTopFuuHan(
        gameInfo: MjGameInfoVo = MjGameInfoVo.Default,
        isRiichi: Boolean = false,
        isIppatsu: Boolean = false,
        isChankan: Boolean = false,
        isHaiTei: Boolean = false,
        isHoutei: Boolean = false,
        isDoubleRiichi: Boolean = false,
    ): MjScoreVo<out MjScoreI> {
        return getPossibleFuuHan(
            gameInfo = gameInfo,
            isRiichi = isRiichi,
            isIppatsu = isIppatsu,
            isChankan = isChankan,
            isHaiTei = isHaiTei,
            isHoutei = isHoutei,
            isDoubleRiichi = isDoubleRiichi
        ).maxBy { it }
    }

    companion object {
        fun parse(teHai: List<MjPai>, agariHai: MjAgariHai, vararg huroBody: MjBody): List<MjTeHai> {
            val paiMap: Map<PaiType, MutableList<MjPai>> = mapOf(
                PaiType.M to mutableListOf(),
                PaiType.P to mutableListOf(),
                PaiType.S to mutableListOf(),
                PaiType.Z to mutableListOf(),
            )

            require(
                huroBody.filterIsInstance<KanBody>().count() + 13 == teHai.size + huroBody.sumOf { it.paiList.size }) {
                "패 개수가 부족합니다."
            }

            (teHai + agariHai.pai).forEach { paiMap[it.type]!!.add(it) }
            paiMap.values.forEach { it.sort() }

            return separateHead(paiMap).map { (head, leftPaiList) -> head to separateBody(leftPaiList) }
                .filterNot { (_, resultBody) -> resultBody.isNullOrEmpty() }
                .flatMap { (head, possibleBodyKatachi) ->
                    possibleBodyKatachi
                        ?.filter { it.size + huroBody.size == 4 }
                        ?.map { MjTeHai(head, it + huroBody, agariHai) } ?: emptyList()
                }
        }

        private fun separateHead(paiMap: Map<PaiType, List<MjPai>>): List<Pair<MjHead, Map<PaiType, List<MjPai>>>> {
            val resultList: MutableList<Pair<MjHead, Map<PaiType, List<MjPai>>>> = mutableListOf()
            paiMap.forEach { (type, list) ->
                list.groupBy { it.num }
                    .filter { (_, samePaiList) -> samePaiList.size >= 2 }
                    .forEach { (_, samePaiList) ->
                        paiMap.toMutableMap().also { result ->
                            val targetHead = samePaiList.take(2)
                            result[type] = list.toMutableList().also {
                                it.remove(samePaiList.first())
                                it.remove(samePaiList.last())
                            }
                            resultList.add(MjHead(targetHead) to result)
                        }
                    }
            }
            return resultList
        }

        /**
         * 패에서 몸통을 분리해 가능한 모든 경우의 수를 출력합니다.
         */
        private fun separateBody(paiMap: Map<PaiType, List<MjPai>>): List<List<MjBody>>? {
            val ziPaiBody = paiMap.getOrElse(PaiType.Z) { emptyList() }.groupBy { it.num }
                .also {
                    if (it.values.any { samePaiList -> samePaiList.size !in 3..4 }) return null
                }
                .map { (_, list) -> MjBody.of(list, false) }

            val bodyByType: Map<PaiType, List<List<MjBody>>> = paiMap.filterNot { it.key == PaiType.Z }
                .map { (type, paiList) -> type to separateBodyR(leftPai = paiList.sorted().toMutableList()) }
                .toMap()

            val manzuBodyList = bodyByType.getOrDefault(PaiType.M, emptyList())
            val souzuBodyList = bodyByType.getOrDefault(PaiType.S, emptyList())
            val pinzuBodyList = bodyByType.getOrDefault(PaiType.P, emptyList())

            return manzuBodyList.flatMap { first ->
                souzuBodyList.flatMap { second ->
                    pinzuBodyList.map { third ->
                        first + second + third + ziPaiBody
                    }
                }
            }
        }

        /**
         * 패에서 몸통을 분리해 가능한 모든 경우의 수를 출력합니다.
         * @param nowPai 현재 빌딩중인 패의 몸통 리스트
         * @param leftPai 몸통을 만들고 남은 패
         * @return List<가능한 몸통 형태>
         */
        private fun separateBodyR(nowPai: List<MjBody> = emptyList(), leftPai: List<MjPai>): List<List<MjBody>> {
            when {
                leftPai.isEmpty() -> return listOf(nowPai)
                leftPai.size < 3 -> return emptyList()

            }

            val firstPai = leftPai.first()

            val nextPai = leftPai.find { it.num == firstPai.num + 1 }
            val nextNextPai = nextPai?.let { leftPai.find { target -> target.num == it.num + 1 } }

            val samePai = leftPai.filter { it.num == firstPai.num }

            val resultList: MutableList<List<MjBody>> = mutableListOf()

            // 슌쯔일 때
            if (nextPai != null && nextNextPai != null) {
                val body = MjBody.of(listOf(firstPai, nextPai, nextNextPai), false)
                resultList += separateBodyR(
                    nowPai = nowPai.toMutableList().also { it.add(body) },
                    leftPai = leftPai.toMutableList().also {
                        it.remove(firstPai)
                        it.remove(nextPai)
                        it.remove(nextNextPai)
                    }
                )
            }

            // 커쯔(3개)일 때
            if (samePai.size >= 3) {
                val toUse = samePai.take(3)
                val body = MjBody.of(toUse, false)
                resultList += separateBodyR(
                    nowPai = nowPai.toMutableList().also { it.add(body) },
                    leftPai = leftPai.toMutableList().also {
                        it.remove(toUse[0])
                        it.remove(toUse[1])
                        it.remove(toUse[2])
                    }
                )
            }
//            깡은 따로 처리
//            // 깡쯔일 때
//            if (samePai.size == 4) {
//                val body = MjBody.of(samePai, false)
//                resultList += separateBodyR(
//                    nowPai = nowPai.toMutableList().also { it.add(body) },
//                    leftPai = leftPai.toMutableList().also {
//                        it.remove(samePai[0])
//                        it.remove(samePai[1])
//                        it.remove(samePai[2])
//                        it.remove(samePai[3])
//                    }
//                )
//            }

            return resultList
        }
    }
}