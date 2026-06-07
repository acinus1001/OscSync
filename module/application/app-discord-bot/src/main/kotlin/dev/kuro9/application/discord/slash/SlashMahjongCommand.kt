package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.service.DiscordUserNameService
import dev.kuro9.domain.mahjong.core.annotation.MahjongInternalApi
import dev.kuro9.domain.mahjong.core.dto.MahjongGameDetailInput
import dev.kuro9.domain.mahjong.core.enums.MahjongLogType
import dev.kuro9.domain.mahjong.core.enums.MahjongSeki
import dev.kuro9.domain.mahjong.core.repository.*
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongScoreSettingService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import dev.kuro9.domain.mahjong.image.service.MahjongScoreGraphService
import dev.kuro9.internal.discord.handler.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.handler.model.EntitySelectInteractionHandler
import dev.kuro9.internal.discord.handler.model.ModalInteractionHandler
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
import dev.kuro9.multiplatform.common.date.util.now
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import kotlinx.coroutines.*
import kotlinx.datetime.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.separator.Separator.Spacing
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.FileUpload
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.RoundingMode
import javax.imageio.ImageIO
import kotlin.time.Clock
import kotlin.time.measureTime

@Component
class SlashMahjongCommand(
    private val mjCalculateService: MjCalculateService,
    private val mjImageService: MjHandPictureService,
    private val mahjongRankService: MahjongRankService,
    private val mahjongStatService: MahjongStatService,
    private val mahjongScoreSettingService: MahjongScoreSettingService,
    private val mahjongScoreGraphService: MahjongScoreGraphService,
    private val userNameService: DiscordUserNameService,
) : SlashCommandComponent, ButtonInteractionHandler, ModalInteractionHandler, EntitySelectInteractionHandler {
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
            subcommand("add", "대국 결과를 기록합니다.") {
                option<User>("user_tou", "동가 (東)", required = true)
                option<Int>("score_tou", "동가 점수", required = true)
                option<User>("user_nan", "남가 (南)", required = true)
                option<Int>("score_nan", "남가 점수", required = true)
                option<User>("user_sha", "서가 (西)", required = true)
                option<Int>("score_sha", "서가 점수", required = true)
                option<User>("user_pei", "북가 (北)", required = true)
                option<Int>("score_pei", "북가 점수", required = true)

                option<File>("image", "대국 결과 이미지 파일", required = false)
            }
        }

        group("stat", "마작 기록을 통한 통계를 가져옵니다.") {
            restrict(guild = true)
            subcommand("month", "월별 유저의 통계를 확인합니다.") {
                option<User>("user", "확인할 유저 선택. 기본값=본인", required = false)
                option<Int>("year", "확인할 년도 선택. 기본값=현재", required = false)
                option<Int>("month", "확인할 월 선택. 기본값=현재", required = false)
            }
            subcommand("all", "전체 기간에 대한 유저의 통계를 확인합니다.") {
                option<User>("user", "확인할 유저 선택. 기본값=본인", required = false)
            }
        }

        group("rank", "마작 기록을 통한 서버 내 순위를 확인합니다.") {
            restrict(guild = true)
            subcommand("month", "월별 서버 내 순위를 확인합니다.") {
                option<Int>("type", "확인할 순위표 종류를 선택. 기본값=포인트(구 우마)", required = false) {
                    choice("포인트(구 우마)", 0)
                    choice("대국수", 1)
                }
                option<Int>("year", "확인할 년도 선택. 기본값=현재", required = false)
                option<Int>("month", "확인할 월 선택. 기본값=현재", required = false)
            }
            subcommand("all", "전체 기간에 대한 서버 내 순위를 확인합니다.") {
                option<Int>("type", "확인할 순위표 종류를 선택. 기본값=포인트(구 우마)", required = false) {
                    choice("포인트(구 우마)", 0)
                    choice("대국수", 1)
                }
            }
        }

        group("admin", "기타 관리를 위한 명령어입니다.") {
            restrict(guild = true, Permission.ADMINISTRATOR)
            subcommand("stat-refresh", "통계 재계산(사용 불가)")
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
        }
    }

    private val buttonPrefix = "mj_button"
    private val modalPrefix = "mj_modal"
    private val selectPrefix = "mj_select"

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = event.asyncDeferReply()

        runCatching {
            when (event.subcommandGroup) {
                "util" -> when (event.subcommandName) {
                    "calculate" -> return calculateScore(event, deferReply)
                    "image" -> return generateImage(event, deferReply)
                }

                "record" -> when (event.subcommandName) {
                    "add" -> return recordAdd(event, deferReply)
                }

                "stat" -> when (event.subcommandName) {
                    "month" -> return recordStatMonth(event, deferReply)
                    "all" -> return recordStatAll(event, deferReply)
                }

                "rank" -> when (event.subcommandName) {
                    "month" -> return recordRankMonth(event, deferReply)
                    "all" -> return recordRankAll(event, deferReply)
                }

                "admin" -> when (event.subcommandName) {
                    "stat-refresh" -> return internalStatRefresh(event, deferReply)
                    "setting" -> return recordSetting(event, deferReply)
                    "setting-list" -> return recordSettingList(event, deferReply)
                }
            }

            throw NotImplementedError("Unknown command=${event.fullCommandName}")
        }.onFailure { t ->
            error(t) { "handle event error: ${event.fullCommandName}" }
            deferReply.await().editOriginalEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    @OptIn(MahjongInternalApi::class)
    private suspend fun internalStatRefresh(
        event: SlashCommandInteractionEvent,
        deferReply: Deferred<InteractionHook>
    ) {
        if (event.user.idLong != 400579163959853056L) {
            Embed {
                title = "403 Forbidden"
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return
        }

        val duration = measureTime {
            withContext(Dispatchers.IO) {
                mahjongStatService.calculateInitial()
            }
        }

        Embed {
            title = "200 OK"
            description = "Job completed in $duration."
            color = Color.GREEN.rgb
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    override suspend fun handleButtonInteraction(event: ButtonInteractionEvent) {

        val (buttonId, data, subData) = event.componentId.removePrefix("${buttonPrefix}_").split("_") + ""

        when (buttonId) {
            "add-delete" -> recordAddDelete(event, data)
            "add-modify-user" -> recordAddModifyUser(event, data)
            "add-modify-score" -> recordAddModifyScore(event, data)
            "month-point" -> updateMonthPointRank(event, data, subData)
            "month-game-count" -> updateMonthGameCountRank(event, data, subData)
            "all-point" -> updateAllPointRank(event, data)
            "all-game-count" -> updateAllGameCountRank(event, data)
        }
    }

    override suspend fun isHandleable(event: ButtonInteractionEvent): Boolean {
        return event.componentId.startsWith(buttonPrefix)
    }

    override suspend fun handleModalInteraction(event: ModalInteractionEvent) {
        val data = event.modalId.split("_")
        val message = event.channel.asTextChannel().retrieveMessageById(data.last()).await()
        info { "modal interaction, message=${message.contentRaw}" }

        val (action, gameId, messageId) = event.modalId.removePrefix("${modalPrefix}_").split("_")
            .also { require(it.size == 3) { "올바른 형식이 아닙니다. modalId: ${event.modalId}" } }

        when (action) {
            "add-delete" -> confirmRecordDelete(event, gameId.toLong(), messageId.toLong())
            "modify-user" -> confirmRecordModifyUser(event, gameId.toLong(), messageId.toLong())
            "modify-score" -> confirmRecordModifyScore(event, gameId.toLong(), messageId.toLong())
        }
    }

    override suspend fun isHandleable(event: ModalInteractionEvent): Boolean {
        return event.modalId.startsWith(modalPrefix)
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "calculate", "image" -> handlePaiAutoComplete(event)
            else -> return
        }
    }

    override suspend fun isHandleable(event: EntitySelectInteractionEvent): Boolean {
        return event.componentId.startsWith(selectPrefix)
    }

    override suspend fun handleEntitySelectInteraction(event: EntitySelectInteractionEvent) {
        val deferReply = event.deferReply()

        val (action, data) = event.componentId.removePrefix("${selectPrefix}_").split("_")

        when (action) {
            "stat-month" -> selectStatMonth(event, deferReply, data)
            "stat-all" -> selectStatAll(event, deferReply, data)
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
                text("-# 설정은 소급 적용되지 않습니다.")
                separator { spacing = Spacing.LARGE }
                buildString {
                    for ((i, setting) in settingList.withIndex()) {
                        appendLine(
                            "- ${if (i == 0) "**[적용중]** " else ""} 시작 `${setting.startScore}` / 반환 `${setting.returnScore}` 우마 `[${setting.umaFirst}, ${setting.umaSecond}, ${setting.umaThird}, ${setting.umaFourth}]` // <t:${
                                setting.createdAt.toInstant(
                                    TimeZone.of("Asia/Seoul")
                                ).epochSeconds
                            }:f> 이후 적용"
                        )
                    }
                }.let(::text)

            }
        }.let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun recordAdd(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val userTou = event.getOption("user_tou")!!.asUser
        val scoreTou = event.getOption("score_tou")!!.asInt
        val userNan = event.getOption("user_nan")!!.asUser
        val scoreNan = event.getOption("score_nan")!!.asInt
        val userSha = event.getOption("user_sha")!!.asUser
        val scoreSha = event.getOption("score_sha")!!.asInt
        val userPei = event.getOption("user_pei")!!.asUser
        val scorePei = event.getOption("score_pei")!!.asInt
        val image = event.getOption("image")?.asAttachment?.also {
            // 확장자 체크
            if (it.isImage.not()) {
                Embed {
                    title = "400 Bad Request"
                    description = "이미지 파일만 업로드 가능합니다."
                    color = Color.RED.rgb
                }.let { deferReply.await().editOriginalEmbeds(it).await() }
                return
            }
        }

        val imageMimeType: MediaType? = image?.fileExtension?.let {
            MediaTypeFactory.getMediaType("image.$it")
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
        }

        val game: MahjongGameEntity = try {
            withContext(Dispatchers.IO) {
                mahjongRankService.save(
                    createdUserId = event.user.idLong,
                    createdGuildId = event.guild!!.idLong,
                    imageUrl = image?.proxyUrl,
                    imageMediaType = imageMimeType,
                    MahjongGameDetailInput(userId = userTou.idLong, score = scoreTou, seki = MahjongSeki.TOU),
                    MahjongGameDetailInput(userId = userNan.idLong, score = scoreNan, seki = MahjongSeki.NAN),
                    MahjongGameDetailInput(userId = userSha.idLong, score = scoreSha, seki = MahjongSeki.SHA),
                    MahjongGameDetailInput(userId = userPei.idLong, score = scorePei, seki = MahjongSeki.PEI),
                )
            }
        } catch (e: IllegalArgumentException) {
            Embed {
                title = "400 Bad Request"
                description = e.message
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return
        }

        suspendTransaction {
            MessageEdit(useComponentsV2 = true) {
                container(uniqueId = 1000) {
                    text("### 패보 기록 완료", uniqueId = 1001)
                    separator(uniqueId = 1101) { spacing = Spacing.LARGE }
                    for ((i, gameDetail) in game.results.withIndex()) {
                        coroutineScope { launch { userNameService.putUserNameCache(gameDetail.userId) } } // userName 캐싱용
                        text(
                            "${i + 1}. [${gameDetail.seki?.kanji}] / <@${gameDetail.userId}> / ${
                                "%,d".format(
                                    gameDetail.score
                                )
                            } / ${
                                "%+,.1f".format(gameDetail.point.setScale(1, RoundingMode.DOWN))
                            }"
                        ) { uniqueId = 1201 + i }
                    }

                    if (game.image != null || game.imageMime != null) {
                        val extension = game.imageMime!!.let { MediaType.parseMediaType(it) }
                        separator(uniqueId = 1301) { spacing = Spacing.LARGE }
                        mediaGallery(uniqueId = 1401) {
                            item(
                                FileUpload.fromData(
                                    game.image!!.bytes,
                                    "image.${extension.getExtensionFromMediaType()}"
                                ),
                            )
                        }
                    }
                    separator(uniqueId = 1501) { spacing = Spacing.LARGE }
                    actionRow(uniqueId = 1600) {
                        dangerButton("${buttonPrefix}_add-delete_${game.id.value}", label = "삭제", uniqueId = 1601)

                        // modal component 5개 제한으로 분리
                        dangerButton(
                            "${buttonPrefix}_add-modify-user_${game.id.value}",
                            label = "대국자 수정",
                            uniqueId = 1602
                        )
                        dangerButton(
                            "${buttonPrefix}_add-modify-score_${game.id.value}",
                            label = "점수 수정",
                            uniqueId = 1603
                        )
                    }
                }
                container(uniqueId = 2000) {
                    text("### 기록 메타데이터", uniqueId = 2001)
                    separator(uniqueId = 2101) { spacing = Spacing.SMALL }
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
                        - 적용된 기록 설정
                          - 우마 : [ $${game.scoreSetting.umaFirst}, $${game.scoreSetting.umaSecond}, $${game.scoreSetting.umaThird}, $${game.scoreSetting.umaFourth} ]
                          - 시작점 / 반환점 : [ $${game.scoreSetting.startScore} / $${game.scoreSetting.returnScore} ]
                        - 기록 일자 : <t:$${game.createdAt.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:f>
                        - 삭제 및 수정 기한 : <t:$${game.updatableUntil.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:f>
                    """.trimIndent().let { text(it, uniqueId = 2201) }
                    text("- 수정 이력", uniqueId = 2202) // 업데이트 로그용 컴포넌트
                }
            }
        }.let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun recordStatMonth(
        event: SlashCommandInteractionEvent,
        deferReply: Deferred<InteractionHook>
    ): Unit = suspendTransaction {
        val user = event.getOption("user")?.asUser ?: event.user
        val year = event.getOption("year")?.asInt ?: LocalDate.now().year
        val month = event.getOption("month")?.asInt ?: LocalDate.now().month.number
        val yearMonth = YearMonth(year, month)

        val stat: MahjongMonthStatEntity? =
            mahjongStatService.getUserStatOrNull(userId = user.idLong, guildId = event.guild!!.idLong)
                ?.monthStats
                ?.firstOrNull { it.yearMonth == yearMonth }

        if (stat == null) {
            Embed {
                title = "404 Not Found"
                description = "해당 유저의 통계가 존재하지 않습니다. 조회 범위 내 해당 유저의 대국이 존재할 경우 잠시 후 다시 시도해주세요."
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return@suspendTransaction
        }

        deferReply
            .await()
            .editOriginal(MessageEdit(useComponentsV2 = true, builder = stat.getMonthStatMessage(user, event.user)))
            .await()
    }

    private suspend fun recordStatAll(
        event: SlashCommandInteractionEvent,
        deferReply: Deferred<InteractionHook>
    ): Unit = suspendTransaction {
        val user = event.getOption("user")?.asUser ?: event.user

        val stat = mahjongStatService.getUserStatOrNull(userId = user.idLong, guildId = event.guild!!.idLong)
        if (stat == null) {
            Embed {
                title = "404 Not Found"
                description = "해당 유저의 통계가 존재하지 않습니다. 조회 범위 내 해당 유저의 대국이 존재할 경우 잠시 후 다시 시도해주세요."
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return@suspendTransaction
        }

        deferReply
            .await()
            .editOriginal(MessageEdit(useComponentsV2 = true, builder = stat.getAllStatMessage(user, event.user)))
            .await()
    }

    private suspend fun recordRankMonth(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val type = event.getOption("type")?.asInt ?: 0
        val year = event.getOption("year")?.asInt ?: LocalDate.now().year
        val month = event.getOption("month")?.asInt ?: LocalDate.now().month.number

        val messageEditData = when (type) {
            0 -> MessageEdit(
                useComponentsV2 = true,
                builder = getMonthPointRankMessage(event.user, event.guild!!.idLong, YearMonth(year, month), 1)
            )

            1 -> MessageEdit(
                useComponentsV2 = true,
                builder = getMonthGameCountRankMessage(event.user, event.guild!!.idLong, YearMonth(year, month), 1)
            )

            else -> {
                throw NotImplementedError("대응되지 않은 타입입니다. type=$type")
            }
        }


        deferReply
            .await()
            .editOriginal(messageEditData)
            .await()
    }

    private suspend fun recordRankAll(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val type = event.getOption("type")?.asInt ?: 0

        val messageEditData = when (type) {
            0 -> MessageEdit(
                useComponentsV2 = true,
                builder = getAllPointRankMessage(event.user, event.guild!!.idLong, 1)
            )

            1 -> MessageEdit(
                useComponentsV2 = true,
                builder = getAllGameCountRankMessage(event.user, event.guild!!.idLong, 1)
            )

            else -> {
                throw NotImplementedError("대응되지 않은 타입입니다. type=$type")
            }
        }


        deferReply
            .await()
            .editOriginal(messageEditData)
            .await()
    }

    private suspend fun updateMonthPointRank(
        event: ButtonInteractionEvent,
        data: String,
        subData: String,
    ) {
        val page = data.toInt()
        val yearMonth = YearMonth.parse(subData)
        val deferReply = event.deferEdit()

        MessageEdit(
            useComponentsV2 = true,
            builder = getMonthPointRankMessage(event.user, event.guild!!.idLong, yearMonth, page)
        ).let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun updateMonthGameCountRank(
        event: ButtonInteractionEvent,
        data: String,
        subData: String,
    ) {
        val page = data.toInt()
        val yearMonth = YearMonth.parse(subData)
        val deferReply = event.deferEdit()

        MessageEdit(
            useComponentsV2 = true,
            builder = getMonthGameCountRankMessage(event.user, event.guild!!.idLong, yearMonth, page)
        ).let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun updateAllPointRank(
        event: ButtonInteractionEvent,
        data: String,
    ) {
        val page = data.toInt()
        val deferReply = event.deferEdit()

        MessageEdit(
            useComponentsV2 = true,
            builder = getAllPointRankMessage(event.user, event.guild!!.idLong, page)
        ).let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun updateAllGameCountRank(
        event: ButtonInteractionEvent,
        data: String,
    ) {
        val page = data.toInt()
        val deferReply = event.deferEdit()

        MessageEdit(
            useComponentsV2 = true,
            builder = getAllGameCountRankMessage(event.user, event.guild!!.idLong, page)
        ).let { deferReply.await().editOriginal(it).await() }
    }

    private suspend fun recordAddDelete(event: ButtonInteractionEvent, data: String) = suspendTransaction {
        val game: MahjongGameEntity =
            handleModifyPermissionAndGetGame(event, data.toLong()) ?: return@suspendTransaction

        val modal = Modal("${modalPrefix}_add-delete_${data}_${event.messageId}", "대국 삭제") {
            this.components += TextDisplay {
                content = buildString {
                    appendLine("# 대국 정보")
                    appendLine("## 순위")
                    for (result in game.results) {
                        appendLine("${result.rank}. ${result.seki?.kanji} : <@${result.userId}> / ${result.score}")
                    }
                    appendLine("## 기타 정보")
                    appendLine("- ID : ${game.id.value}")
                    appendLine("- 기록일 : <t:${game.createdAt.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:f>")
                    appendLine("- 기록자 : <@${game.createdBy}>")
                }
            }
            this.components += TextDisplay {
                content = "위 정보가 정확하고 삭제가 필요한 기록이라면 전송을 클릭해 주세요. 삭제된 기록은 복구할 수 없습니다."
            }
        }

        event.replyModal(modal).await()
    }

    private suspend fun recordAddModifyUser(event: ButtonInteractionEvent, data: String) = suspendTransaction {
        val game: MahjongGameEntity =
            handleModifyPermissionAndGetGame(event, data.toLong()) ?: return@suspendTransaction

        val tou = game.results.first { it.seki == MahjongSeki.TOU }
        val nan = game.results.first { it.seki == MahjongSeki.NAN }
        val sha = game.results.first { it.seki == MahjongSeki.SHA }
        val pei = game.results.first { it.seki == MahjongSeki.PEI }

        val modal = Modal("${modalPrefix}_modify-user_${data}_${event.messageId}", "대국자 수정") {
            label("동가 (東) : ${tou.score}") {
                child = EntitySelectMenu("user_tou", types = listOf(EntitySelectMenu.SelectTarget.USER)) {
                    setDefaultValues(EntitySelectMenu.DefaultValue.user(tou.userId))
                }
            }
            label("남가 (南) : ${nan.score}") {
                child = EntitySelectMenu("user_nan", types = listOf(EntitySelectMenu.SelectTarget.USER)) {
                    setDefaultValues(EntitySelectMenu.DefaultValue.user(nan.userId))
                }
            }
            label("서가 (西) : ${sha.score}") {
                child = EntitySelectMenu("user_sha", types = listOf(EntitySelectMenu.SelectTarget.USER)) {
                    setDefaultValues(EntitySelectMenu.DefaultValue.user(sha.userId))
                }
            }
            label("북가 (北) : ${pei.score}") {
                child = EntitySelectMenu("user_pei", types = listOf(EntitySelectMenu.SelectTarget.USER)) {
                    setDefaultValues(EntitySelectMenu.DefaultValue.user(pei.userId))
                }
            }
        }

        event.replyModal(modal).await()
    }

    private suspend fun recordAddModifyScore(event: ButtonInteractionEvent, data: String) = suspendTransaction {
        val game: MahjongGameEntity =
            handleModifyPermissionAndGetGame(event, data.toLong()) ?: return@suspendTransaction

        val tou = game.results.first { it.seki == MahjongSeki.TOU }
        val nan = game.results.first { it.seki == MahjongSeki.NAN }
        val sha = game.results.first { it.seki == MahjongSeki.SHA }
        val pei = game.results.first { it.seki == MahjongSeki.PEI }

        val touName = userNameService.getUserName(tou.userId)
        val nanName = userNameService.getUserName(nan.userId)
        val shaName = userNameService.getUserName(sha.userId)
        val peiName = userNameService.getUserName(pei.userId)

        val modal = Modal("${modalPrefix}_modify-score_${data}_${event.messageId}", "점수 수정") {
            label("동가 (東) : $touName") {
                child = TextInput("score_tou", style = TextInputStyle.SHORT) {
                    value = tou.score.toString()
                    requiredLength = 1..10
                }
            }
            label("남가 (南) : $nanName") {
                child = TextInput("score_nan", style = TextInputStyle.SHORT) {
                    value = nan.score.toString()
                    requiredLength = 1..10
                }
            }
            label("서가 (西) : $shaName") {
                child = TextInput("score_sha", style = TextInputStyle.SHORT) {
                    value = sha.score.toString()
                    requiredLength = 1..10
                }
            }
            label("북가 (北) : $peiName") {
                child = TextInput("score_pei", style = TextInputStyle.SHORT) {
                    value = pei.score.toString()
                    requiredLength = 1..10
                }
            }
        }

        event.replyModal(modal).await()
    }

    private suspend fun confirmRecordDelete(
        event: ModalInteractionEvent,
        gameId: Long,
        messageId: Long,
    ) = suspendTransaction {
        handleModifyPermissionAndGetGame(event, gameId) ?: return@suspendTransaction
        val defer = event.deferEdit().await()
        val originalMessageAction = event.channel.asTextChannel().retrieveMessageById(messageId)

        mahjongRankService.delete(gameId, event.user.idLong)
        val gameEditLog: List<MahjongGameEditLogEntity> =
            mahjongRankService.getGameById(gameId, nullsOnDeleted = false)?.editLogs?.toList() ?: emptyList()

        val originalMessage = originalMessageAction.await()
        val (mainComponent, metadataComponent) = originalMessage.componentTree.asDisabled().components
        val newMain = mainComponent.asContainer()
            .apply {
                withSpoiler(true)
                withAccentColor(Color.RED)
            }
            .replace { old ->
                when (old.uniqueId) {
                    1001 -> TextDisplay("### [삭제됨] 패보 기록 완료", uniqueId = 1001)
                    1501, 1600, 1601, 1602, 1603 -> null
                    else -> old
                }
            }
        val newMeta = metadataComponent.asContainer().replace { old ->
            when (old.uniqueId) {
                2202 -> TextDisplay(uniqueId = 2202) {
                    content = buildString {
                        var initModify = false
                        for (log in gameEditLog) when (log.type) {
                            MahjongLogType.NEW -> continue
                            MahjongLogType.MODIFY -> {
                                if (initModify.not()) {
                                    initModify = true
                                    appendLine("- 수정 이력")
                                }
                                appendLine("  - <@${event.user.id}> / <t:${Clock.System.now().epochSeconds}:f>")
                            }

                            MahjongLogType.DELETE -> appendLine("- 삭제 일자 : <t:${Clock.System.now().epochSeconds}:f>\n- 삭제자 : <@${event.user.id}>")
                        }
                    }
                }

                else -> old
            }
        }
        MessageEdit(useComponentsV2 = true) {
            components += newMain
            components += newMeta
        }.let { defer.editOriginal(it).await() }
        originalMessage.replyEmbeds(Embed {
            title = "200 OK"
            description = "패보 기록이 삭제되었습니다."
        }).await()
    }

    private suspend fun confirmRecordModifyUser(
        event: ModalInteractionEvent,
        gameId: Long,
        messageId: Long,
    ) = suspendTransaction {
        val game = handleModifyPermissionAndGetGame(event, gameId) ?: return@suspendTransaction
        val defer = event.deferEdit().await()
        val originalMessage = event.channel.asTextChannel().retrieveMessageById(messageId).await()

        val touUser = event.interaction.getValue("user_tou")!!.asMentions.usersBag.first()
        val nanUser = event.interaction.getValue("user_nan")!!.asMentions.usersBag.first()
        val shaUser = event.interaction.getValue("user_sha")!!.asMentions.usersBag.first()
        val peiUser = event.interaction.getValue("user_pei")!!.asMentions.usersBag.first()

        val (_, modifiedResults) = try {
            mahjongRankService.modify(
                id = game.id.value,
                modifyUserId = event.user.idLong,
                MahjongGameDetailInput(
                    userId = touUser.idLong,
                    score = game.results.first { it.seki == MahjongSeki.TOU }.score,
                    seki = MahjongSeki.TOU
                ),
                MahjongGameDetailInput(
                    userId = nanUser.idLong,
                    score = game.results.first { it.seki == MahjongSeki.NAN }.score,
                    seki = MahjongSeki.NAN
                ),
                MahjongGameDetailInput(
                    userId = shaUser.idLong,
                    score = game.results.first { it.seki == MahjongSeki.SHA }.score,
                    seki = MahjongSeki.SHA
                ),
                MahjongGameDetailInput(
                    userId = peiUser.idLong,
                    score = game.results.first { it.seki == MahjongSeki.PEI }.score,
                    seki = MahjongSeki.PEI
                ),
            )
        } catch (e: IllegalArgumentException) {
            info { "modify error: $e" }
            Embed {
                title = "400 Bad Request"
                description = e.message
                color = Color.RED.rgb
            }.let { originalMessage.replyEmbeds(it).await() }
            return@suspendTransaction
        }

        val scoreComponentMap = modifiedResults.withIndex().associate { (i, gameDetail) ->
            coroutineScope { launch { userNameService.putUserNameCache(gameDetail.userId) } } // userName 캐싱용

            1201 + i to TextDisplay(
                "${i + 1}. [${gameDetail.seki?.kanji}] / <@${gameDetail.userId}> / ${
                    "%,d".format(
                        gameDetail.score
                    )
                } / ${
                    "%+,.1f".format(gameDetail.point.setScale(1, RoundingMode.DOWN))
                }"
            ) { uniqueId = 1201 + i }
        }

        val (mainComponent, metadataComponent) = originalMessage.componentTree.components
        val newMain = mainComponent.asContainer()
            .replace { old ->
                scoreComponentMap[old.uniqueId] ?: old
            }
        val newMeta = metadataComponent.asContainer().replace { old ->
            when (old.uniqueId) {
                2202 -> TextDisplay(uniqueId = 2202) {
                    content = buildString {
                        var initModify = false
                        for (log in mahjongRankService.getGameLogById(game.id.value)) when (log.type) {
                            MahjongLogType.NEW -> continue
                            MahjongLogType.MODIFY -> {
                                if (initModify.not()) {
                                    initModify = true
                                    appendLine("- 수정 이력")
                                }
                                appendLine("  - <@${event.user.id}> / <t:${Clock.System.now().epochSeconds}:f>")
                            }

                            MahjongLogType.DELETE -> appendLine("- 삭제 일자 : <t:${Clock.System.now().epochSeconds}:f>\n- 삭제자 : <@${event.user.id}>")
                        }
                    }
                }

                else -> old
            }
        }
        MessageEdit(useComponentsV2 = true) {
            components += newMain
            components += newMeta
        }.let { defer.editOriginal(it).await() }
        originalMessage.replyEmbeds(Embed {
            title = "200 OK"
            description = "패보 기록이 수정되었습니다."
        }).await()
    }

    private suspend fun confirmRecordModifyScore(
        event: ModalInteractionEvent,
        gameId: Long,
        messageId: Long,
    ) = suspendTransaction {
        val game = handleModifyPermissionAndGetGame(event, gameId) ?: return@suspendTransaction
        val defer = event.deferEdit().await()
        val originalMessage = event.channel.asTextChannel().retrieveMessageById(messageId).await()

        val touScore =
            event.interaction.getValue("score_tou")!!.asString.filter { it.isDigit() || it == '-' }.toIntOrNull()
        val nanScore =
            event.interaction.getValue("score_nan")!!.asString.filter { it.isDigit() || it == '-' }.toIntOrNull()
        val shaScore =
            event.interaction.getValue("score_sha")!!.asString.filter { it.isDigit() || it == '-' }.toIntOrNull()
        val peiScore =
            event.interaction.getValue("score_pei")!!.asString.filter { it.isDigit() || it == '-' }.toIntOrNull()

        if (listOf(touScore, nanScore, shaScore, peiScore).any { it == null }) {
            Embed {
                title = "400 Bad Request"
                description = "점수를 읽을 수 없습니다. 숫자 및 '-' 기호만 입력해 주세요. e.g. -13100"
                color = Color.RED.rgb
            }.let { originalMessage.replyEmbeds(it).await() }
            return@suspendTransaction
        }

        val (_, modifiedResults) = try {
            withContext(Dispatchers.IO) {
                mahjongRankService.modify(
                    id = game.id.value,
                    modifyUserId = event.user.idLong,
                    MahjongGameDetailInput(
                        userId = game.results.first { it.seki == MahjongSeki.TOU }.userId,
                        score = touScore!!,
                        seki = MahjongSeki.TOU
                    ),
                    MahjongGameDetailInput(
                        userId = game.results.first { it.seki == MahjongSeki.NAN }.userId,
                        score = nanScore!!,
                        seki = MahjongSeki.NAN
                    ),
                    MahjongGameDetailInput(
                        userId = game.results.first { it.seki == MahjongSeki.SHA }.userId,
                        score = shaScore!!,
                        seki = MahjongSeki.SHA
                    ),
                    MahjongGameDetailInput(
                        userId = game.results.first { it.seki == MahjongSeki.PEI }.userId,
                        score = peiScore!!,
                        seki = MahjongSeki.PEI
                    ),
                )
            }
        } catch (e: IllegalArgumentException) {
            info { "modify error: $e" }
            Embed {
                title = "400 Bad Request"
                description = e.message
                color = Color.RED.rgb
            }.let { originalMessage.replyEmbeds(it).await() }
            return@suspendTransaction
        }

        val scoreComponentMap = modifiedResults.withIndex().associate { (i, gameDetail) ->
            coroutineScope { launch { userNameService.putUserNameCache(gameDetail.userId) } } // userName 캐싱용

            1201 + i to TextDisplay(
                "${i + 1}. [${gameDetail.seki?.kanji}] / <@${gameDetail.userId}> / ${
                    "%,d".format(
                        gameDetail.score
                    )
                } / ${
                    "%+,.1f".format(gameDetail.point.setScale(1, RoundingMode.DOWN))
                }"
            ) { uniqueId = 1201 + i }
        }

        val (mainComponent, metadataComponent) = originalMessage.componentTree.components
        val newMain = mainComponent.asContainer()
            .replace { old ->
                scoreComponentMap[old.uniqueId] ?: old
            }
        val newMeta = metadataComponent.asContainer().replace { old ->
            when (old.uniqueId) {
                2202 -> TextDisplay(uniqueId = 2202) {
                    content = buildString {
                        var initModify = false
                        for (log in mahjongRankService.getGameLogById(game.id.value)) when (log.type) {
                            MahjongLogType.NEW -> continue
                            MahjongLogType.MODIFY -> {
                                if (initModify.not()) {
                                    initModify = true
                                    appendLine("- 수정 이력")
                                }
                                appendLine("  - <@${event.user.id}> / <t:${Clock.System.now().epochSeconds}:f>")
                            }

                            MahjongLogType.DELETE -> appendLine("- 삭제 일자 : <t:${Clock.System.now().epochSeconds}:f>\n- 삭제자 : <@${event.user.id}>")
                        }
                    }
                }

                else -> old
            }
        }
        MessageEdit(useComponentsV2 = true) {
            components += newMain
            components += newMeta
        }.let { defer.editOriginal(it).await() }
        originalMessage.replyEmbeds(Embed {
            title = "200 OK"
            description = "패보 기록이 수정되었습니다."
        }).await()
    }

    private suspend fun selectStatMonth(
        event: EntitySelectInteractionEvent,
        deferReply: ReplyCallbackAction,
        data: String
    ): Unit =
        suspendTransaction {
            val userSelected = event.values.first().idLong
            val user = event.jda.retrieveUserById(userSelected)
            val yearMonth = YearMonth.parse(data)
            info { "userSelected: $userSelected" }

            val stat: MahjongMonthStatEntity? =
                mahjongStatService.getUserStatOrNull(userId = userSelected, guildId = event.guild!!.idLong)
                    ?.monthStats
                    ?.firstOrNull { it.yearMonth == yearMonth }

            if (stat == null) {
                Embed {
                    title = "404 Not Found"
                    description = "해당 유저의 통계가 존재하지 않습니다. 조회 범위 내 해당 유저의 대국이 존재할 경우 잠시 후 다시 시도해주세요."
                    color = Color.RED.rgb
                }.let { deferReply.await().editOriginalEmbeds(it).await() }
                return@suspendTransaction
            }

            deferReply.await().sendMessage(
                MessageCreate(
                    useComponentsV2 = true,
                    builder = stat.getMonthStatMessage(user.await(), event.user)
                )
            ).await()
        }

    private suspend fun selectStatAll(
        event: EntitySelectInteractionEvent,
        deferReply: ReplyCallbackAction,
        data: String
    ): Unit =
        suspendTransaction {
            val userSelected = event.values.first().idLong
            val user = event.jda.retrieveUserById(userSelected)
            info { "userSelected: $userSelected" }

            val stat = mahjongStatService.getUserStatOrNull(userId = userSelected, guildId = event.guild!!.idLong)

            if (stat == null) {
                Embed {
                    title = "404 Not Found"
                    description = "해당 유저의 통계가 존재하지 않습니다. 조회 범위 내 해당 유저의 대국이 존재할 경우 잠시 후 다시 시도해주세요."
                    color = Color.RED.rgb
                }.let { deferReply.await().editOriginalEmbeds(it).await() }
                return@suspendTransaction
            }

            deferReply.await().sendMessage(
                MessageCreate(
                    useComponentsV2 = true,
                    builder = stat.getAllStatMessage(user.await(), event.user)
                )
            ).await()
        }

    /**
     * 권한이 없다면 ephemeral로 응답 후 null 반환합니다.
     */
    private suspend fun handleModifyPermissionAndGetGame(
        event: IReplyCallback,
        gameId: Long
    ): MahjongGameEntity? {
        val game: MahjongGameEntity? = withContext(Dispatchers.IO) {
            mahjongRankService.getGameById(gameId)
        }

        if (game == null) {
            event.replyEmbeds(Embed {
                title = "404 Not Found";
                description =
                    "해당 게임을 찾을 수 없습니다. 이미 삭제되었거나 외부에서 변경되었을 경우일 수 있습니다."
                color = Color.RED.rgb
            }).setEphemeral(true).await()  // 게임 존재하지 않음. 삭제 등의 케이스.
            return null
        }

        val isAllowed = (event.member!!.hasPermission(Permission.ADMINISTRATOR)).or(
            game.results.any { it.userId == event.user.idLong }
        )

        if (!isAllowed) {
            event.replyEmbeds(Embed {
                title = "403 Forbidden"
                description =
                    "해당 대국에 대한 수정 권한이 없습니다. 어드민 및 대국 참여자만 수정 가능합니다."
                color = Color.RED.rgb
            }).setEphemeral(true).await()
            return null
        }

        if (!event.member!!.hasPermission(Permission.ADMINISTRATOR) && game.updatableUntil < LocalDateTime.now()) {
            event.replyEmbeds(Embed {
                title = "403 Forbidden";
                description = "대국 수정 기간이 만료되었습니다. 관리자에게 문의해 주세요."
                color = Color.RED.rgb
            }).setEphemeral(true).await()
            return null
        }

        return game
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

    private fun MediaType.getExtensionFromMediaType(): String {
        return when (this) {
            MediaType.IMAGE_JPEG -> "jpg"
            MediaType.IMAGE_PNG -> "png"
            MediaType.IMAGE_GIF -> "gif"
            MediaType.parseMediaType("image/webp") -> "webp"
            MediaType.parseMediaType("image/heic") -> "heic"
            MediaType.parseMediaType("image/tiff") -> "tiff"
            else -> subtype // 기본적으로 subtype 반환
        }
    }

    private suspend fun MahjongMonthStatEntity.getMonthStatMessage(
        user: User,
        eventCaller: User
    ): InlineMessage<*>.() -> Unit =
        suspendTransaction {
            val userName = userNameService.getUserName(user.idLong)
            val recentData = mahjongRankService
                .getUserGamesAtRange(
                    userId = user.idLong,
                    guildId = totalStat.guildId,
                    start = yearMonth.firstDay,
                    endInclusive = yearMonth.lastDay,
                )
                .orderBy(MahjongGames.id to SortOrder.DESC)
                .limit(10)
                .reversed()
                .map { game ->
                    val userResult = game.results.first { it.userId == user.idLong }
                    userResult.rank to (userResult.score >= 50_000)
                }

            val image = mahjongScoreGraphService.scoreGraphGen(recentData)

            return@suspendTransaction {
                container {
                    accentColor = when (user.idLong) {
                        400579163959853056L -> Color.WHITE
                        else -> Color.GRAY
                    }
                    section {
                        accessory = Thumbnail(user.effectiveAvatarUrl)
                        text("### [#$umaRank] $userName 님의 통계")
                        text("-# $yearMonth 범위")
                        text("-# 요청자 : ${eventCaller.asMention}")
                    }
                    separator { spacing = Spacing.LARGE }
                    mediaGallery {
                        item(FileUpload.fromData(image)).also { image.delete() }
                    }
                    separator { spacing = Spacing.LARGE }

                    text {
                        content = buildString {
                            appendLine("- 서버 내 포인트 순위 : ${umaRank}위")
                            appendLine("- 총 포인트 : ${"%+,.1f".format(totalUmaSum)}")
                            appendLine("- 총합 국 수 : ${totalGameCount}회")
                            appendLine("- 순위별 확률 및 횟수")

                            appendLine("  - 1위 : ${"%.2f".format(firstPlaceRate)}% (${firstPlaceCount}회)")
                            appendLine("  - 2위 : ${"%.2f".format(secondPlaceRate)}% (${secondPlaceCount}회)")
                            appendLine("  - 3위 : ${"%.2f".format(thirdPlaceRate)}% (${thirdPlaceCount}회)")
                            appendLine("  - 4위 : ${"%.2f".format(fourthPlaceRate)}% (${fourthPlaceCount}회)")
                            appendLine("  - 들통(토비) : ${"%.2f".format(tobiRate)}% (${tobiCount}회)")

                            appendLine("- 평균 순위 : ${"%.2f".format(avgPlace)}위")
                            appendLine("- 평균 포인트 : ${"%+,.1f".format(avgUma)}")
                        }
                    }
                    separator { spacing = Spacing.LARGE }
                    text("-# 마지막 업데이트 일자 : <t:${updatedAt.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:R>")
                }
                actionRow {
                    entitySelectMenu(
                        types = listOf(EntitySelectMenu.SelectTarget.USER),
                        customId = "${selectPrefix}_stat-month_${yearMonth}"
                    ) {
                        setDefaultValues(EntitySelectMenu.DefaultValue.user(user.idLong))
                    }
                }
            }
        }

    private suspend fun MahjongTotalStatEntity.getAllStatMessage(
        user: User,
        eventCaller: User
    ): InlineMessage<*>.() -> Unit =
        suspendTransaction {
            val userName = userNameService.getUserName(user.idLong)
            val recentData = mahjongRankService
                .getUserGamesAtRange(
                    userId = user.idLong,
                    guildId = guildId,
                )
                .orderBy(MahjongGames.id to SortOrder.DESC)
                .limit(10)
                .reversed()
                .map { game ->
                    val userResult = game.results.first { it.userId == user.idLong }
                    userResult.rank to (userResult.score >= 50_000)
                }

            val image = mahjongScoreGraphService.scoreGraphGen(recentData)

            return@suspendTransaction {
                container {
                    accentColor = when (user.idLong) {
                        400579163959853056L -> Color.WHITE
                        else -> Color.GRAY
                    }
                    section {
                        accessory = Thumbnail(user.effectiveAvatarUrl)
                        text("### [#$umaRank] $userName 님의 통계")
                        text("-# 전체 기록 범위")
                        text("-# 요청자 : ${eventCaller.asMention}")
                    }
                    separator { spacing = Spacing.LARGE }
                    mediaGallery {
                        item(FileUpload.fromData(image)).also { image.delete() }
                    }
                    separator { spacing = Spacing.LARGE }

                    text {
                        content = buildString {
                            appendLine("- 서버 내 포인트 순위 : ${umaRank}위")
                            appendLine("- 총 포인트 : ${"%+,.1f".format(totalUmaSum)}")
                            appendLine("- 총합 국 수 : ${totalGameCount}회")
                            appendLine("- 순위별 확률 및 횟수")

                            appendLine("  - 1위 : ${"%.2f".format(firstPlaceRate)}% (${firstPlaceCount}회)")
                            appendLine("  - 2위 : ${"%.2f".format(secondPlaceRate)}% (${secondPlaceCount}회)")
                            appendLine("  - 3위 : ${"%.2f".format(thirdPlaceRate)}% (${thirdPlaceCount}회)")
                            appendLine("  - 4위 : ${"%.2f".format(fourthPlaceRate)}% (${fourthPlaceCount}회)")
                            appendLine("  - 들통(토비) : ${"%.2f".format(tobiRate)}% (${tobiCount}회)")

                            appendLine("- 평균 순위 : ${"%.2f".format(avgPlace)}위")
                            appendLine("- 평균 포인트 : ${"%+,.1f".format(avgUma)}")
                        }
                    }
                    separator { spacing = Spacing.LARGE }
                    text("-# 마지막 업데이트 일자 : <t:${updatedAt.toInstant(TimeZone.of("Asia/Seoul")).epochSeconds}:R>")
                }
                actionRow {
                    entitySelectMenu(
                        types = listOf(EntitySelectMenu.SelectTarget.USER),
                        customId = "${selectPrefix}_stat-all_"
                    ) {
                        setDefaultValues(EntitySelectMenu.DefaultValue.user(user.idLong))
                    }
                }
            }
        }

    private fun getMonthPointRankMessage(
        eventCaller: User,
        guildId: Long,
        yearMonth: YearMonth,
        page: Int,
    ): InlineMessage<*>.() -> Unit = result@{
        require(page >= 1)
        // size = 30
        val (userList, count) = mahjongStatService.getMonthPointRank(
            guildId = guildId,
            n = 30,
            offset = (page - 1) * 30L,
            yearMonth = yearMonth,
        )
        val maxPage = ((count / 30) + if (count % 30 == 0L) 0 else 1).toInt()

        val userListWithId: List<Pair<MahjongMonthStatEntity, Long>> = runBlocking {
            suspendTransaction {
                userList.map { user -> user to user.totalStat.userId }
            }
        }

        return@result container {
            mentions { user(eventCaller) }
            accentColor = Color.GRAY

            text("### 포인트 순위표")
            text("-# $yearMonth 범위")
            text("-# 요청자 : ${eventCaller.asMention}")
            separator { spacing = Spacing.LARGE }
            text("**순위**\t**포인트**\t\t\t\t\t\t**플레이어**")
            separator { spacing = Spacing.SMALL }
            text {
                content = buildString {
                    for ((user, userId) in userListWithId) {
                        appendLine("${user.umaRank}\\.\t\t**${"%+,.1f".format(user.totalUmaSum)}**\t\t\t\t\t<@${userId}>")
                    }
                }.takeIf { it.isNotBlank() } ?: "-# 데이터가 없습니다."
            }
            separator { spacing = Spacing.LARGE }
            if (maxPage > 0)
                actionRow {
                    if (page > 1)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-point_${page.minus(1).coerceIn(1..maxPage)}_${yearMonth}",
                            label = "<"
                        )
                    primaryButton(
                        customId = "${buttonPrefix}_month-point_${page.coerceIn(1..maxPage)}_${yearMonth}",
                        label = "PAGE $page / $maxPage",
                        emoji = Emoji.fromUnicode("\uD83D\uDD04")
                    )
                    if (page < maxPage)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-point_${page.plus(1).coerceIn(1..maxPage)}_${yearMonth}",
                            label = ">"
                        )
                }
        }
    }

    private fun getAllPointRankMessage(
        eventCaller: User,
        guildId: Long,
        page: Int,
    ): InlineMessage<*>.() -> Unit = result@{
        require(page >= 1)
        // size = 30
        val (userList, count) = mahjongStatService.getAllPointRank(
            guildId = guildId,
            n = 30,
            offset = (page - 1) * 30L,
        )
        val maxPage = ((count / 30) + if (count % 30 == 0L) 0 else 1).toInt()

        val userListWithId: List<Pair<MahjongMonthStatEntity, Long>> = runBlocking {
            suspendTransaction {
                userList.map { user -> user to user.totalStat.userId }
            }
        }

        return@result container {
            mentions { user(eventCaller) }
            accentColor = Color.GRAY

            text("### 포인트 순위표")
            text("-# 전체 기록 범위")
            text("-# 요청자 : ${eventCaller.asMention}")
            separator { spacing = Spacing.LARGE }
            text("**순위**\t**포인트**\t\t\t\t\t\t**플레이어**")
            separator { spacing = Spacing.SMALL }
            text {
                content = buildString {
                    for ((user, userId) in userListWithId) {
                        appendLine("${user.umaRank}\\.\t\t**${"%+,.1f".format(user.totalUmaSum)}**\t\t\t\t\t<@${userId}>")
                    }
                }.takeIf { it.isNotBlank() } ?: "-# 데이터가 없습니다."
            }
            separator { spacing = Spacing.LARGE }
            if (maxPage > 0)
                actionRow {
                    if (page > 1)
                        secondaryButton(
                            customId = "${buttonPrefix}_all-point_${page.minus(1).coerceIn(1..maxPage)}",
                            label = "<"
                        )
                    primaryButton(
                        customId = "${buttonPrefix}_all-point_${page.coerceIn(1..maxPage)}",
                        label = "PAGE $page / $maxPage",
                        emoji = Emoji.fromUnicode("\uD83D\uDD04")
                    )
                    if (page < maxPage)
                        secondaryButton(
                            customId = "${buttonPrefix}_all-point_${page.plus(1).coerceIn(1..maxPage)}",
                            label = ">"
                        )
                }
        }
    }

    private fun getMonthGameCountRankMessage(
        eventCaller: User,
        guildId: Long,
        yearMonth: YearMonth,
        page: Int,
    ): InlineMessage<*>.() -> Unit = result@{
        require(page >= 1)
        // size = 30
        val (userList, count) = mahjongStatService.getMonthGameCountRank(
            guildId = guildId,
            n = 30,
            offset = (page - 1) * 30L,
            yearMonth = yearMonth,
        )
        val maxPage = ((count / 30) + if (count % 30 == 0L) 0 else 1).toInt()

        val userListWithId: List<Pair<MahjongMonthStatEntity, Long>> = runBlocking {
            suspendTransaction {
                userList.map { user -> user to user.totalStat.userId }
            }
        }

        return@result container {
            mentions { user(eventCaller) }
            accentColor = Color.GRAY

            text("### 대국수 순위표")
            text("-# $yearMonth 범위")
            text("-# 요청자 : ${eventCaller.asMention}")
            separator { spacing = Spacing.LARGE }
            text("**순위**\t**대국수**\t\t\t\t\t\t**플레이어**")
            separator { spacing = Spacing.SMALL }
            text {
                content = buildString {
                    for ((user, userId) in userListWithId) {
                        appendLine("${user.gameCountRank}\\.\t\t**${"%,d".format(user.totalGameCount)}**\t\t\t\t\t<@${userId}>")
                    }
                }.takeIf { it.isNotBlank() } ?: "-# 데이터가 없습니다."
            }
            separator { spacing = Spacing.LARGE }
            if (maxPage > 0)
                actionRow {
                    if (page > 1)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-game-count_${
                                page.minus(1).coerceIn(1..maxPage)
                            }_${yearMonth}",
                            label = "<"
                        )
                    primaryButton(
                        customId = "${buttonPrefix}_month-game-count_${page.coerceIn(1..maxPage)}_${yearMonth}",
                        label = "PAGE $page / $maxPage",
                        emoji = Emoji.fromUnicode("\uD83D\uDD04")
                    )
                    if (page < maxPage)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-game-count_${
                                page.plus(1).coerceIn(1..maxPage)
                            }_${yearMonth}",
                            label = ">"
                        )
                }
        }
    }

    private fun getAllGameCountRankMessage(
        eventCaller: User,
        guildId: Long,
        page: Int,
    ): InlineMessage<*>.() -> Unit = result@{
        require(page >= 1)
        // size = 30
        val (userList, count) = mahjongStatService.getAllGameCountRank(
            guildId = guildId,
            n = 30,
            offset = (page - 1) * 30L,
        )
        val maxPage = ((count / 30) + if (count % 30 == 0L) 0 else 1).toInt()

        val userListWithId: List<Pair<MahjongMonthStatEntity, Long>> = runBlocking {
            suspendTransaction {
                userList.map { user -> user to user.totalStat.userId }
            }
        }

        return@result container {
            mentions { user(eventCaller) }
            accentColor = Color.GRAY

            text("### 대국수 순위표")
            text("-# 전체 기록 범위")
            text("-# 요청자 : ${eventCaller.asMention}")
            separator { spacing = Spacing.LARGE }
            text("**순위**\t**대국수**\t\t\t\t\t\t**플레이어**")
            separator { spacing = Spacing.SMALL }
            text {
                content = buildString {
                    for ((user, userId) in userListWithId) {
                        appendLine("${user.gameCountRank}\\.\t\t**${"%,d".format(user.totalGameCount)}**\t\t\t\t\t<@${userId}>")
                    }
                }.takeIf { it.isNotBlank() } ?: "-# 데이터가 없습니다."
            }
            separator { spacing = Spacing.LARGE }
            if (maxPage > 0)
                actionRow {
                    if (page > 1)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-game-count_${page.minus(1).coerceIn(1..maxPage)}",
                            label = "<"
                        )
                    primaryButton(
                        customId = "${buttonPrefix}_month-game-count_${page.coerceIn(1..maxPage)}",
                        label = "PAGE $page / $maxPage",
                        emoji = Emoji.fromUnicode("\uD83D\uDD04")
                    )
                    if (page < maxPage)
                        secondaryButton(
                            customId = "${buttonPrefix}_month-game-count_${page.plus(1).coerceIn(1..maxPage)}",
                            label = ">"
                        )
                }
        }
    }
}