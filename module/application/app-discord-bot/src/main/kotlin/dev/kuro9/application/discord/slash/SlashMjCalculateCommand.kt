package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.mahjong.calc.enums.MjYaku
import dev.kuro9.internal.mahjong.calc.model.MjTeHai
import dev.kuro9.internal.mahjong.calc.service.MjCalculateService
import dev.kuro9.internal.mahjong.calc.utils.MjScoreI
import dev.kuro9.internal.mahjong.calc.utils.MjScoreUtil
import dev.kuro9.internal.mahjong.calc.utils.MjScoreVo
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashMjCalculateCommand(private val mjCalculateService: MjCalculateService) : SlashCommandComponent {
    override val commandData: SlashCommandData = Command("mjc", "마작 패 계산기") {
        subcommand("score", "부수/판수 계산.") {
            option<String>("tehai", "손패. 123m123s12333t77z 과 같은 형식으로 입력하세요.", required = true)
            option<String>("tsumo", "쯔모한 패. 1m 과 같은 형식으로 입력하세요. ron 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("ron", "론한 패. 1m 과 같은 형식으로 입력하세요. tsumo 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("huro", "론한 패. 123m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        runCatching {
            when (event.subcommandName) {
                "score" -> calculateScore(event)

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t ->
            event.replyEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    private suspend fun calculateScore(event: SlashCommandInteractionEvent) {
        val deferReply = event.asyncDeferReply()

        val tehai = event.getOption("tehai")!!.asString
        val tsumo = event.getOption("tsumo")?.asString
        val ron = event.getOption("ron")?.asString
        val huro = event.getOption("huro")?.asString

        // validate input
        if (!((tsumo != null) xor (ron != null))) {
            throw IllegalArgumentException("쯔모와 론 옵션 중 하나의 옵션에만 입력하십시오.")
        }

        val huroBody = huro?.removeSurrounding(" ")?.split(" ")?.toTypedArray()

        val parsedTeHai: MjTeHai = mjCalculateService.parseTeHai(
            teHaiStr = tehai,
            agariHaiStr = ron ?: tsumo!!,
            isRon = ron != null,
            huroBody = huroBody ?: emptyArray()
        )

        val score: MjScoreVo<out MjScoreI> = parsedTeHai.getTopFuuHan()

        val resultEmbed = Embed {
            title = "Result"
            description = "`$parsedTeHai`"

            field {
                name = "점수"
                value = "`[${
                    when (val scoreType = score.score) {
                        is MjScoreI.Ron -> "론"
                        is MjScoreI.Tsumo -> "쯔모"
                        is MjScoreI.NoYaku -> if (scoreType.isRon) "론" else "쯔모"
                    }
                }] ${score.score}`"
                inline = false
            }

            field {
                name = "부수 / 판수"
                value = (score.scoreEnum.toKrString()?.let { "`[${it}]" } ?: "`") + " ${score.han}판 / ${score.fuu}부`"
                inline = false
            }

            if (score.yakuSet.isNotEmpty()) {
                field {
                    name = "손역"
                    value = score.yakuSet.joinToString(
                        "\n",
                        prefix = "```\n",
                        postfix = "\n```"
                    ) { yaku -> "[${if (parsedTeHai.isHuro && yaku.kuiSagari) yaku.han - 1 else yaku.han}] ${yaku.toKrString()}" }
                }
            }
            color = Color.GREEN.rgb
        }.let { mutableListOf(it) }

        if (score.scoreEnum == MjScoreUtil.MjScore.ELSE) run {
            val scoreTable = MjScoreUtil.scoreTable[score.fuu] ?: return@run

            resultEmbed += Embed {
                title = "부수/판수 테이블"
                description = "${score.fuu}부 점수 표"
                field {
                    name = "자의 경우"
                    value = scoreTable.mapIndexed { index, (ron, tsumo) ->
                        "[${index + 1}판] 론(${ron.first}) 쯔모(${tsumo.koScore} / ${tsumo.oyaScore})"
                    }.joinToString("\n", prefix = "```\n", postfix = "\n```")
                    inline = false
                }
                field {
                    name = "오야의 경우"
                    value = scoreTable.mapIndexed { index, (ron, tsumo) ->
                        "[${index + 1}판] 론(${ron.second}) 쯔모(${tsumo.oyaScore} ALL)"
                    }.joinToString("\n", prefix = "```\n", postfix = "\n```")
                    inline = false
                }
            }
        }

        deferReply.await().editOriginalEmbeds(resultEmbed).await()
    }

    private fun MjScoreUtil.MjScore.toKrString(): String? = when (this) {
        MjScoreUtil.MjScore.YAKUMAN -> "역만"
        MjScoreUtil.MjScore.SANBAIMAN -> "삼배만"
        MjScoreUtil.MjScore.BAIMAN -> "배만"
        MjScoreUtil.MjScore.HANEMAN -> "하네만"
        MjScoreUtil.MjScore.MANKAN -> "만관"
        MjScoreUtil.MjScore.ELSE -> null
    }

    private fun MjYaku.toKrString(): String = when (this) {
        MjYaku.RIICHI -> "리치"
        MjYaku.IPPATSU -> "일발"
        MjYaku.TSUMO -> "쯔모"
        MjYaku.YAKU_BAKASE -> "역패: 장풍"
        MjYaku.YAKU_ZIKASE -> "역패: 자풍"
        MjYaku.YAKU_HAKU -> "역패: 백"
        MjYaku.YAKU_HATSU -> "역패: 발"
        MjYaku.YAKU_CHUU -> "역패: 중"
        MjYaku.TANYAO -> "탕야오"
        MjYaku.PINFU -> "핑후"
        MjYaku.IPECO -> "이페코"
        MjYaku.CHANKAN -> "창깡"
        MjYaku.HAITEI -> "해저로월"
        MjYaku.HOUTEI -> "하저로어"
        MjYaku.DOUBLE_RIICHI -> "더블 리치"
        MjYaku.CHANTA -> "찬타"
        MjYaku.HONROUTOU -> "혼노두"
        MjYaku.SANSHOKU_DOUJUU -> "삼색동순"
        MjYaku.SANSHOKU_DOUKOU -> "삼색동각"
        MjYaku.ITTKITSUKAN -> "일기통관"
        MjYaku.TOITOI -> "또이또이"
        MjYaku.SANANKOU -> "산안커"
        MjYaku.SANKANTSU -> "산깡쯔"
        MjYaku.CHITOITSU -> "치또이"
        MjYaku.SHOUSANGEN -> "소삼원"
        MjYaku.JUNCHANTA -> "준찬타"
        MjYaku.HONITSU -> "혼일색"
        MjYaku.RYANPEKO -> "량페코"
        MjYaku.CHINITSU -> "청일색"
        MjYaku.TENHOU -> "천화"
        MjYaku.CHIHOU -> "지화"
        MjYaku.SUANKOU -> "스안커"
        MjYaku.KOKUSHI -> "국사무쌍"
        MjYaku.DAISANGEN -> "대삼원"
        MjYaku.TSUISO -> "자일색"
        MjYaku.RYUISO -> "녹일색"
        MjYaku.CHINROTO -> "청노두"
        MjYaku.SYOUSUSI -> "소사희"
        MjYaku.CHUREN -> "구련보등"
        MjYaku.SUKANTSU -> "스깡쯔"
        MjYaku.DAISUSI -> "대사희"
        MjYaku.SUANKOU_TANKI -> "사암각 단기"
        MjYaku.KOKUSHI_13MEN -> "국사무쌍 13면대기"
        MjYaku.CHUREN_9MEN -> "순정구련보등"
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {

            is IllegalArgumentException -> Embed {
                title = "Invalid Input"
                description = "잘못된 입력입니다. ${t.message ?: ""}"
                color = Color.RED.rgb
            }

            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }

    private suspend fun SlashCommandInteractionEvent.asyncDeferReply(isEphemeral: Boolean = false): Deferred<InteractionHook> {
        return coroutineScope {
            async { deferReply(isEphemeral).await() }
        }
    }
}