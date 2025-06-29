package dev.kuro9.application.discord.slash

import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.mahjong.calc.enums.MjKaze
import dev.kuro9.internal.mahjong.calc.enums.MjYaku
import dev.kuro9.internal.mahjong.calc.model.MjGameInfoVo
import dev.kuro9.internal.mahjong.calc.model.MjTeHai
import dev.kuro9.internal.mahjong.calc.service.MjCalculateService
import dev.kuro9.internal.mahjong.calc.utils.MjScoreI
import dev.kuro9.internal.mahjong.calc.utils.MjScoreUtil
import dev.kuro9.internal.mahjong.calc.utils.MjScoreVo
import dev.kuro9.internal.mahjong.image.MjHandPictureService
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Component
class SlashMjCalculateCommand(
    private val mjCalculateService: MjCalculateService,
    private val mjImageService: MjHandPictureService,
) : SlashCommandComponent {
    override val commandData: SlashCommandData = Command("mj", "마작 관련 명령어") {
        subcommand("calculate", "부수/판수, 역 계산.") {
            option<String>("tehai", "손패. 123m123s12333p77z 과 같은 형식으로 입력하세요.", required = true)
            option<String>("tsumo", "쯔모한 패. 1m 과 같은 형식으로 입력하세요. ron 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("ron", "론한 패. 1m 과 같은 형식으로 입력하세요. tsumo 파라미터와 동시에 입력하지 마십시오.", required = false)
            option<String>("huro", "후로한 패. 123m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
            option<String>("ankang", "안깡한 패. 1111m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
            option<String>("bakaze", "장풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
            option<String>("zikaze", "자풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = event.asyncDeferReply()

        runCatching {
            when (event.subcommandName) {
                "calculate" -> calculateScore(event, deferReply)

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t ->
            deferReply.await().editOriginalEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "calculate" -> handlePaiAutoComplete(event)
            else -> return
        }
    }

    private suspend fun handlePaiAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name != "bakaze" && event.focusedOption.name != "zikaze") return

        val choices = listOf(
            "東(동 / 1z)",
            "南(남 / 2z)",
            "西(서 / 3z)",
            "北(북 / 4z)"
        )

        val filtered = choices.filter { it.contains(event.focusedOption.value) }
            .takeIf { it.isNotEmpty() }
            ?: choices

        event.replyChoiceStrings(filtered).await()
    }

    private suspend fun calculateScore(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {

        val tehai = event.getOption("tehai")!!.asString
        val tsumo = event.getOption("tsumo")?.asString
        val ron = event.getOption("ron")?.asString
        val huro = event.getOption("huro")?.asString
        val ankang = event.getOption("ankang")?.asString
        val bakaze = (event.getOption("bakaze")?.asString ?: "동").let(::toKaze)
        val zikaze = (event.getOption("zikaze")?.asString ?: "동").let(::toKaze)

        // validate input
        if (!((tsumo != null) xor (ron != null))) {
            throw IllegalArgumentException("쯔모와 론 옵션 중 하나의 옵션에만 입력하십시오.")
        }

        val huroBody = huro?.removeSurrounding(" ")?.split(" ")?.toTypedArray()
        val ankangBody = ankang?.removeSurrounding(" ")?.split(" ")?.toTypedArray()

        val gameInfo = MjGameInfoVo.of(zikaze = zikaze, bakaze = bakaze)
        val parsedTeHai: MjTeHai? = mjCalculateService.parseTeHai(
            teHaiStr = tehai,
            agariHaiStr = ron ?: tsumo!!,
            isRon = ron != null,
            huroBody = huroBody ?: emptyArray(),
            anKanBody = ankangBody ?: emptyArray(),
            gameInfo = gameInfo
        )

        requireNotNull(parsedTeHai) { "완성된 손패가 아닙니다." }

        val handImage = withContext(Dispatchers.Default) {
            async {
                val image = mjImageService.getHandPicture(parsedTeHai, gameInfo)
                ByteArrayOutputStream().use { baos ->
                    ImageIO.write(image, "png", baos)
                    baos.toByteArray()
                }
            }
        }

        val score: MjScoreVo<out MjScoreI> = parsedTeHai.getTopFuuHan(gameInfo = gameInfo)

        val resultEmbed = Embed {
            title = "Result"
            description = "`$parsedTeHai`"

            field {
                name = "점수"
                value = when (val scoreType = score.score) {
                    is MjScoreI.Ron -> "[론]\n자: ${score.getKoRonScore()}\n오야: ${score.getOyaRonScore()}"
                    is MjScoreI.Tsumo -> "[쯔모]\n자: ${score.score}\n오야: ${(score.score as MjScoreI.Tsumo).oyaScore} ALL"
                    is MjScoreI.NoYaku -> (if (scoreType.isRon) "[론]" else "[쯔모]") + " 역 없음"
                }.let { "```$it```" }
                inline = false
            }

            field {
                name = "부수 / 판수"
                value =
                    """```${score.scoreEnum.toKrString()?.let { "[${it}] " } ?: ""}${score.han}판 / ${score.fuu}부```"""
                inline = false
            }

            if (score.yakuSet.isNotEmpty()) {
                field {
                    name = "손역"
                    value = score.yakuSet.joinToString(
                        "\n",
                        prefix = "```\n",
                        postfix = "\n```"
                    ) { yaku -> "[${if (parsedTeHai.isHuro && yaku.kuiSagari) yaku.han - 1 else yaku.han}판] ${yaku.toKrString()}" }
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
        deferReply.await().sendFiles(FileUpload.fromData(handImage.await(), "hand.png")).await()
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
        MjYaku.TSUMO -> "멘젠 쯔모"
        MjYaku.YAKU_BAKASE -> "역패: 장풍패"
        MjYaku.YAKU_ZIKASE -> "역패: 자풍패"
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
        MjYaku.SUANKOU_TANKI -> "스안커 단기"
        MjYaku.KOKUSHI_13MEN -> "국사무쌍 13면대기"
        MjYaku.CHUREN_9MEN -> "순정구련보등"
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {

            is IllegalArgumentException -> Embed {
                title = "Invalid Input"
                description = "잘못된 입력입니다. ${t.message?.take(2500) ?: ""}"
                color = Color.RED.rgb
            }

            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }.also { error(t) { "handled err" } }

    private suspend fun SlashCommandInteractionEvent.asyncDeferReply(isEphemeral: Boolean = false): Deferred<InteractionHook> {
        return coroutineScope {
            async { deferReply(isEphemeral).await() }
        }
    }

    private fun toKaze(str: String): MjKaze {
        return when {
            str.contains("동") -> MjKaze.TOU
            str.contains("남") -> MjKaze.NAN
            str.contains("서") -> MjKaze.SHA
            str.contains("북") -> MjKaze.PEI

            str.contains("東") -> MjKaze.TOU
            str.contains("南") -> MjKaze.NAN
            str.contains("西") -> MjKaze.SHA
            str.contains("北") -> MjKaze.PEI

            str.contains("1z") -> MjKaze.TOU
            str.contains("2z") -> MjKaze.NAN
            str.contains("3z") -> MjKaze.SHA
            str.contains("4z") -> MjKaze.PEI
            else -> throw IllegalArgumentException("알 수 없는 풍패입니다. 입력값: $str")
        }
    }
}