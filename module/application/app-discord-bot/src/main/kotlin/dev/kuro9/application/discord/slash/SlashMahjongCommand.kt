package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.service.DiscordUserNameService
import dev.kuro9.domain.mahjong.core.repository.MahjongGameEntity
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongScoreSettingService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
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
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageEdit
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.yearMonth
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.separator.Separator.Spacing
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import javax.imageio.ImageIO

@Component
class SlashMahjongCommand(
    private val mjCalculateService: MjCalculateService,
    private val mjImageService: MjHandPictureService,
    private val mahjongRankService: MahjongRankService,
    private val mahjongStatService: MahjongStatService,
    private val mahjongScoreSettingService: MahjongScoreSettingService,
    private val userNameService: DiscordUserNameService,
) : SlashCommandComponent {
    override val commandData: SlashCommandData = Command("mj", "마작 관련 명령어") {
        group("util", "마작 관련 유틸성 명령어") {
            subcommand("calculate", "부수/판수, 역 계산.") {
                option<String>("tehai", "손패. 123m123s12333p77z 과 같은 형식으로 입력하세요.", required = true)
                option<String>("tsumo", "쯔모한 패. 1m 과 같은 형식으로 입력하세요. ron 파라미터와 동시에 입력하지 마십시오.", required = false)
                option<String>("ron", "론한 패. 1m 과 같은 형식으로 입력하세요. tsumo 파라미터와 동시에 입력하지 마십시오.", required = false)
                option<String>("huro", "후로한 패. 123m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
                option<String>("ankang", "안깡한 패. 1111m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
                option<String>("bakaze", "장풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
                option<String>("zikaze", "자풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
            }

            subcommand("image", "손패 이미지를 생성합니다.") {
                option<String>("tehai", "손패. 123m123s12333p77z 과 같은 형식으로 입력하세요.", required = true)
                option<String>("tsumo", "쯔모한 패. 1m 과 같은 형식으로 입력하세요. ron 파라미터와 동시에 입력하지 마십시오.", required = false)
                option<String>("ron", "론한 패. 1m 과 같은 형식으로 입력하세요. tsumo 파라미터와 동시에 입력하지 마십시오.", required = false)
                option<String>("huro", "후로한 패. 123m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
                option<String>("ankang", "안깡한 패. 1111m 4444s 와 같이 공백으로 구분해 입력하세요.", required = false)
                option<String>("bakaze", "장풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
                option<String>("zikaze", "자풍패. 동/남/서/북 중 하나. 기본값=동", required = false, autocomplete = true)
            }
        }

        group("record", "마작 기록 관련 명령어") {
            restrict(guild = true)
            subcommand("setting", "기록 시 반영될 우마/오카 및 반환점 등의 설정을 변경합니다. 설정은 소급적용되지 않습니다.") {
                restrict(guild = true, Permission.ADMINISTRATOR)
                option<Int>("uma_1st", "1위 우마. (참고용 : 작혼 = +15)", required = true)
                option<Int>("uma_2nd", "2위 우마. (참고용 : 작혼 = +5)", required = true)
                option<Int>("uma_3rd", "3위 우마. (참고용 : 작혼 = -5)", required = true)
                option<Int>("uma_4th", "4위 우마. (참고용 : 작혼 = -15)", required = true)
                option<Int>(
                    "start-point",
                    "시작점수. (참고용 : 작혼 = 25000) // 시작점수와 반환점수가 다를 경우 1위 오카 지급이 활성화됩니다.",
                    required = true
                )
                option<Int>(
                    "return-point",
                    "반환점수. (참고용 : 작혼 = 25000) // 시작점수와 반환점수가 다를 경우 1위 오카 지급이 활성화됩니다.",
                    required = true
                )
            }
            subcommand("setting-list", "현재 서버에 적용 중인 기록 설정을 가져옵니다.")
            subcommand("add", "대국 결과를 기록합니다.") {
                option<User>("user_1st", "1위 유저", required = true)
                option<Int>("score_1st", "1위 점수", required = true)
                option<User>("user_2nd", "2위 유저", required = true)
                option<Int>("score_2nd", "2위 점수", required = true)
                option<User>("user_3rd", "3위 유저", required = true)
                option<Int>("score_3rd", "3위 점수", required = true)
                option<User>("user_4th", "4위 유저", required = true)
                option<Int>("score_4th", "4위 점수", required = true)
            }
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = event.asyncDeferReply()

        runCatching {
            when (event.subcommandGroup) {
                "util" -> when (event.subcommandName) {
                    "calculate" -> return calculateScore(event, deferReply)
                    "image" -> return generateImage(event, deferReply)
                }

                "record" -> when (event.subcommandName) {
                    "setting" -> return recordSetting(event, deferReply)
                    "setting-list" -> return recordSettingList(event, deferReply)
                    "test", "add" -> return recordAdd(event, deferReply)
                }
            }

            throw NotImplementedError("Unknown command=${event.fullCommandName}")
        }.onFailure { t ->
            error(t) { "handle event error: ${event.fullCommandName}" }
            deferReply.await().editOriginalEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "calculate", "image" -> handlePaiAutoComplete(event)
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

    private suspend fun generateImage(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {

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

        val image = mjImageService.getHandPicture(parsedTeHai, gameInfo)
        val handImage = ByteArrayOutputStream().use { baos ->
            ImageIO.write(image, "png", baos)
            baos.toByteArray()
        }

        val resultEmbed = Embed {
            title = "Result"
            description = "`$parsedTeHai`"
            color = Color.GREEN.rgb
        }.let { mutableListOf(it) }

        val fileUpload = FileUpload.fromData(handImage, "hand.png")
        deferReply.await().sendFiles(fileUpload).await()
        deferReply.await().editOriginalEmbeds(resultEmbed).await()
    }

    private suspend fun recordSetting(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val uma1st = event.getOption("uma_1st")?.asInt ?: 15
        val uma2nd = event.getOption("uma_2nd")?.asInt ?: 5
        val uma3rd = event.getOption("uma_3rd")?.asInt ?: -5
        val uma4th = event.getOption("uma_4th")?.asInt ?: -15
        val startPoint = event.getOption("start-point")?.asInt ?: 25000
        val returnPoint = event.getOption("return-point")?.asInt ?: 25000

        withContext(Dispatchers.IO) {
            mahjongScoreSettingService.postNewScoreSetting(
                guildId = event.guild!!.idLong,
                userId = event.user.idLong,
                startScore = startPoint,
                returnScore = returnPoint,
                umaFirst = uma1st,
                umaSecond = uma2nd,
                umaThird = uma3rd,
                umaFourth = uma4th,
            )
        }

        Embed {
            title = "200 OK"
            description = "다음 기록부터 적용됩니다."
            field {
                name = "설정 추가자"
                value = event.user.asMention
                inline = true
            }
            field {
                name = "시작점수"
                value = startPoint.toString()
                inline = true
            }
            field {
                name = "반환점수"
                value = returnPoint.toString()
                inline = true
            }
            field {
                name = "우마"
                value = "`[${uma1st}, ${uma2nd}, ${uma3rd}, ${uma4th}]`"
                inline = true
            }
            field {
                name = "오카"
                value = "`${((returnPoint - startPoint) * 4 / 1000.0)}`"
                inline = true
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun recordSettingList(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {

        val settingList = withContext(Dispatchers.IO) {
            mahjongScoreSettingService.getAllScoreSetting(event.guild!!.idLong)
        }

        MessageEdit(useComponentsV2 = true) {
            container {
                text("### 기록 Score 설정 변경 내역")
                separator { spacing = Spacing.LARGE }
                for ((i, setting) in settingList.withIndex()) {
                    text(
                        "- ${if (i == 0) "**[적용중]** " else ""} 시작 `${setting.startScore}` / 반환 `${setting.returnScore}` 우마 `[${setting.umaFirst}, ${setting.umaSecond}, ${setting.umaThird}, ${setting.umaFourth}]` // <t:${
                            setting.createdAt.toInstant(
                                TimeZone.of("Asia/Seoul")
                            ).epochSeconds
                        }:f> 이후 적용"
                    )
                }
            }
        }.let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun recordAdd(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val user1st = event.getOption("user_1st")!!.asUser
        val user2nd = event.getOption("user_2nd")!!.asUser
        val user3rd = event.getOption("user_3rd")!!.asUser
        val user4th = event.getOption("user_4th")!!.asUser
        val score1st = event.getOption("score_1st")!!.asInt
        val score2nd = event.getOption("score_2nd")!!.asInt
        val score3rd = event.getOption("score_3rd")!!.asInt
        val score4th = event.getOption("score_4th")!!.asInt

        val game: MahjongGameEntity = withContext(Dispatchers.IO) {
            mahjongRankService.save(
                createdUserId = event.user.idLong,
                createdGuildId = event.guild!!.idLong,
                firstScore = score1st,
                secondScore = score2nd,
                thirdScore = score3rd,
                fourthScore = score4th,
                firstUserId = user1st.idLong,
                secondUserId = user2nd.idLong,
                thirdUserId = user3rd.idLong,
                fourthUserId = user4th.idLong,
            )
        }

        suspendTransaction {
            MessageEdit(useComponentsV2 = true) {
                container {
                    text("### 패보 기록 완료")
                    separator { spacing = Spacing.LARGE }
                    for ((i, gameDetail) in game.results.withIndex()) {
                        text(
                            "${i + 1}. ${userNameService.getUserName(gameDetail.userId)} / ${"%,d".format(gameDetail.score)} / ${
                                "%+,.1f".format(gameDetail.point.setScale(1, RoundingMode.DOWN))
                            }"
                        )
                    }
                }
                container {
                    text("### 기록 메타데이터")
                    separator { spacing = Spacing.SMALL }
                    $$"""
                        - 기록 ID : $${game.id.value}
                        - 국 번호 (전체) : 제 $${
                        mahjongStatService.getGameCount(
                            guildId = event.guild!!.idLong,
                            ofGameId = game.id.value
                        )
                    } 국
                        - 국 번호 (금월) : 제 $${
                        mahjongStatService.getMonthGameCount(
                            guildId = event.guild!!.idLong,
                            yearMonth = game.createdAt.date.yearMonth,
                            ofGameId = game.id.value
                        )
                    } 국
                        - 기록 설정
                          - 우마 : [ $${game.scoreSetting.umaFirst}, $${game.scoreSetting.umaSecond}, $${game.scoreSetting.umaThird}, $${game.scoreSetting.umaFourth} ]
                          - 시작점 / 반환점 : [ $${game.scoreSetting.startScore} / $${game.scoreSetting.returnScore} ]
                        - 기록 일자 : <t:$${game.createdAt.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:f>
                    """.trimIndent().let(::text)
                }
            }
        }.let { deferReply.await().editOriginal(it).await() }
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