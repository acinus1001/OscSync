package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.exception.DiscordArgumentNotProvidedException
import dev.kuro9.application.discord.exception.GuildOnlyCommandException
import dev.kuro9.application.discord.service.DiscordUserNameService
import dev.kuro9.domain.chess.dto.ChessComGuildRank
import dev.kuro9.domain.chess.dto.ChessComUserStat
import dev.kuro9.domain.chess.enums.EloType
import dev.kuro9.domain.chess.exception.ChessComUserNotRegisteredException
import dev.kuro9.domain.chess.service.ChessComUserProfileService
import dev.kuro9.domain.chess.service.ChessComUserService
import dev.kuro9.domain.error.handler.discord.DiscordCommandErrorHandle
import dev.kuro9.internal.chess.api.exception.ChessApiFailureException
import dev.kuro9.internal.discord.message.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.multiplatform.common.chess.util.extractSanListFromPgn
import dev.kuro9.multiplatform.common.chess.util.getFen
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import io.github.harryjhin.slf4j.extension.info
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashChessCommand(
    private val chessUserService: ChessComUserService,
    private val chessUserProfileService: ChessComUserProfileService,
    private val discordUserService: DiscordUserNameService,
) : SlashCommandComponent, ButtonInteractionHandler {

    private val pgnButtonPrefix = "chess_pgn_"

    override val commandData = Command("chess", "체스 관련 기능입니다.") {
        group("user", "chess.com 유저 관련 기능입니다.") {
            subcommand("register", "chess.com 유저 정보를 등록/업데이트 합니다.") {
                option<String>("username", "chess.com 유저네임", required = true)
            }
            subcommand("unregister", "chess.com 유저 정보 등록을 해제합니다.")
            subcommand("profile", "chess.com 프로파일을 확인합니다. 기본값=본인") {
                option<String>("username", "chess.com 유저네임", required = false)
            }
            subcommand("rank", "체스 레이팅 순위를 확인합니다. 기본값=Rapid") {
                option<String>("type", "레이팅 타입", required = false) {
                    EloType.entries.forEach { type ->
                        choice(type.displayName, type.name)
                    }
                }
            }
        }

        group("util", "체스 관련 유틸 기능입니다.") {
            subcommand("board", "체스보드 이미지를 주어진 FEN 및 PGN을 통해 생성합니다.") {
                option<String>("pgn", "전체 PGN string", required = true)
            }
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = CoroutineScope(Dispatchers.IO).async {
            event.deferReply().await()
        }

        withContext(Dispatchers.IO) {
            // 미리 캐시에 저장
            launch {
                discordUserService.getUserName(event.user.idLong)
            }
        }

        runCatching {
            when (event.subcommandGroup) {
                "user" -> when (event.subcommandName) {
                    "register" -> registerUser(event, deferReply)
                    "unregister" -> unregisterUser(event, deferReply)
                    "profile" -> getUserProfile(event, deferReply)
                    "rank" -> getRank(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }

                "util" -> when (event.subcommandName) {
                    "board" -> getBoardImage(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t: Throwable ->
            error(t) { "handle event error: ${event.fullCommandName}" }
            deferReply.await()
                .editOriginalEmbeds(getDefaultExceptionEmbed(t))
                .await()
        }
    }

    private suspend fun registerUser(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val username =
            event.getOption("username")?.asString ?: throw DiscordArgumentNotProvidedException(listOf("username"))

        val profile: ChessComUserStat.Guest = try {
            chessUserProfileService.getUserProfile(username)
        } catch (e: ChessApiFailureException) {
            Embed {
                title = "유저를 찾을 수 없습니다."
                description = "`${e.apiMessage}`"
                color = Color.YELLOW.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }

            return
        }

        chessUserService.upsertUser(
            userId = event.user.idLong,
            guildId = event.guild?.idLong,
            chessUserName = profile.username,
            chessUserUrl = profile.profileUrl,
            chessProfilePic = profile.avatarUrl,
        )

        profile.eloMap.entries.forEach { (type, elo) ->
            chessUserService.insertElo(
                userId = event.user.idLong,
                eloType = type,
                elo = elo
            )
        }

        Embed {
            title = "200 OK"
            description = "등록 완료"
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun unregisterUser(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        chessUserService.deleteUser(event.user.idLong)

        Embed {
            title = "200 OK"
            description = "삭제 완료"
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun getUserProfile(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val providedUsername = event.getOption("username")?.asString

        val profile: ChessComUserStat = providedUsername?.let { chessUserProfileService.getUserProfile(it) }
            ?: chessUserProfileService.getUserProfile(event.user.idLong)

        Embed {
            title = profile.username
            url = profile.profileUrl
            thumbnail = profile.avatarUrl

            profile.eloMap.entries
                .filter { (_, elo) -> elo > 0 }
                .forEach { (type, elo) ->
                    field {
                        name = "${type.displayName} 레이팅"
                        value = elo.toString()
                        inline = true
                    }
                }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun getRank(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val providedType = event.getOption("type")?.asString?.let { EloType.valueOf(it) } ?: EloType.RAPID
        val guildId = event.guild?.idLong ?: throw GuildOnlyCommandException()

        val rank: ChessComGuildRank = chessUserService.getRank(guildId, providedType)

        val nameMap = rank.rankList.associate { userInfo: ChessComGuildRank.UserInfo ->
            userInfo.userId to discordUserService.getUserName(userInfo.userId)
        }

        val leaderBoardString = rank.rankList.joinToString(
            separator = "\n",
            prefix = "```\n[등수] 닉네임 ( chess.com 닉네임 ) : ELO\n\n",
            postfix = "\n```"
        ) { userInfo: ChessComGuildRank.UserInfo ->
            "[${userInfo.guildRank}] ${nameMap[userInfo.userId]} ( ${userInfo.chessComUserName} ) : ${userInfo.elo}"
        }

        Embed {
            title = "${providedType.displayName} 레이팅"
            description = leaderBoardString
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun getBoardImage(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val pgn = event.getOption("pgn")?.asString ?: throw DiscordArgumentNotProvidedException(listOf("pgn"))

        val fenRegex = """\[FEN\s+"(.+?)"]""".toRegex()
        val matchResult = fenRegex.find(pgn)
        val fen = matchResult?.groupValues?.getOrNull(1) ?: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val sanList = extractSanListFromPgn(pgn)

//        val boardFen = getFen(fen, sanList)
        val isWhiteStarting: Boolean = fen.split(" ")[1] == "w"
        val imageUrl = URLBuilder("https://www.chess.com/dynboard")
            .apply {
                parameters["fen"] = fen
                parameters["size"] = "3"
                parameters["coordinates"] = "true"
            }
            .toString()

        info { "imageUrl: $imageUrl" }

        val fullMoveCount = fen.split(" ")[5].toInt()

        val chunkedSanList: List<List<String>> = sanList.let {
            return@let when (isWhiteStarting) {
                true -> it.chunked(2)
                false -> {
                    val firstBlackMove = it.firstOrNull()?.let(::listOf)?.let(::listOf) ?: listOf()
                    val otherMove = it.drop(1).chunked(2)
                    firstBlackMove + otherMove
                }
            }
        }
            .filter { it.isNotEmpty() }

        Embed {
            title = "FEN Preview"
            image = imageUrl
            description = "FEN: `$fen`"
            field {
                name = "기보"
                value = buildString {
                    for ((index, chunk) in chunkedSanList.withIndex()) {
                        if (index == 0 && isWhiteStarting.not()) {
                            append(fullMoveCount)
                            append("... ")
                            append(chunk.first())
                            append("\n")
                            continue
                        }

                        append(fullMoveCount + index)
                        append(". ")
                        append(chunk.joinToString(" "))
                        append("\n")
                    }
                }
            }
            footer {
                name = "[INDEX=0] [FLIP=false] [FEN=$fen]"
            }
        }.let {
            deferReply.await().run {
                editOriginalEmbeds(it).await()

                editOriginalComponents(
                    ActionRow.of(
                        Button.secondary("${pgnButtonPrefix}board_start", "<<"),
                        Button.secondary("${pgnButtonPrefix}board_prev", "<"),
                        Button.primary("${pgnButtonPrefix}board_flip", "FLIP"),
                        Button.secondary("${pgnButtonPrefix}board_next", ">"),
                        Button.secondary("${pgnButtonPrefix}board_end", ">>"),
                    ),
                ).await()
            }
        }


    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed = when (t) {

        is NotImplementedError -> Embed {
            title = "Not Implemented"
            description = "This command is not implemented. Contact <@400579163959853056> to report."
            color = Color.RED.rgb
        }

        is ChessApiFailureException -> when (t.httpStatus) {
            404 -> Embed {
                title = "유저를 찾을 수 없습니다."
                description = "`${t.apiMessage}`"
                color = Color.YELLOW.rgb
            }

            else -> throw t
        }

        is ChessComUserNotRegisteredException -> Embed {
            title = "등록된 사용자가 아닙니다."
            description = "`/chess user register` 로 등록 후 사용해주세요."
            color = Color.RED.rgb
        }

        is DiscordArgumentNotProvidedException -> Embed {
            title = "필요 파라미터가 입력되지 않았습니다."
            description = "`${t.argumentNames}`"
            color = Color.RED.rgb
        }

        is GuildOnlyCommandException -> Embed {
            title = "다이렉트 메시지에서 사용 불가한 명령어입니다."
            color = Color.RED.rgb
        }

        is ClientRequestException -> {
            if (t.response.status != HttpStatusCode.NotFound) throw t

            Embed {
                title = "404 Not Found"
                description = "존재하지 않는 요청입니다."
                color = Color.YELLOW.rgb
            }
        }

        else -> throw t
    }

    override suspend fun isHandleable(event: ButtonInteractionEvent): Boolean {
        return event.componentId.startsWith(pgnButtonPrefix)
    }

    @DiscordCommandErrorHandle
    override suspend fun handleButtonInteraction(event: ButtonInteractionEvent) {
        val deferEdit = event.deferEdit().await()

        // 형식: chess_pgn_<buttonId>_<각 event에 대해 필요한 data>
        val (buttonId, data) = event.componentId.removePrefix(pgnButtonPrefix).split("_")
            .also { require(it.size == 2) { "올바른 형식이 아닙니다. buttonId: ${event.componentId}" } }

        when (buttonId) {
            "board" -> handleBoardButton(event, deferEdit, data)
        }
    }

    private suspend fun handleBoardButton(event: ButtonInteractionEvent, hook: InteractionHook, data: String) {
        val originalMessage = event.message

        val sanList =
            originalMessage.embeds.first().fields.firstOrNull { it.name == "기보" }?.value?.filter { it != '`' }
                ?.split("\n")


        val metaData = originalMessage.embeds.first().footer!!.text!!

        val matches = """\[(\w+)=([^]]+)]""".toRegex().findAll(metaData)
        val metadataMap = matches.associate { matchResult ->
            val (key, value) = matchResult.destructured
            key to value
        }

        when (data) {
            "prev", "next", "start", "end" -> Unit
            "flip" -> Unit
        }

        fun countMovesInSanList(sanList: List<String>?): Int {
            if (sanList.isNullOrEmpty()) return 0

            var moveCount = 0

            sanList.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.contains("...") -> {
                        // 1... e5 와 같은 형식 (흑 이동 하나만 있는 경우)
                        moveCount++
                    }

                    trimmed.contains(". ") -> {
                        // 1. e4 e5 와 같은 형식 (백과 흑 이동이 모두 있을 수 있음)
                        val movePart = trimmed.substringAfter(". ").trim()
                        val moves = movePart.split(" ")

                        // 백 이동 계산
                        if (moves.isNotEmpty() && moves[0].isNotEmpty()) {
                            moveCount++
                        }

                        // 흑 이동 계산
                        if (moves.size > 1 && moves[1].isNotEmpty()) {
                            moveCount++
                        }
                    }
                }
            }

            return moveCount
        }

        val sanTotalCount = countMovesInSanList(sanList)
        val index = metadataMap["INDEX"]?.toIntOrNull()?.let {
            when (data) {
                "prev" -> it - 1
                "next" -> it + 1
                "start" -> 0
                "end" -> sanTotalCount
                else -> it
            }
        }?.coerceIn(0..sanTotalCount) ?: 0


        val sanString: String? = if (sanList == null || (sanList.firstOrNull() == null)) {
            null
        } else {
            val isBlackFirst = sanList.firstOrNull()?.contains("...") ?: false

            // 모든 움직임을 순서대로 추출
            val allMoves = buildList {
                sanList.forEach { line ->
                    val trimmed = line.trim()
                    when {
                        trimmed.contains("...") -> {
                            // 1... e5 와 같은 형식 처리
                            add(trimmed.substringAfter("... ").trim())
                        }

                        trimmed.contains(". ") -> {
                            // 1. e4 e5 와 같은 형식 처리
                            val movePart = trimmed.substringAfter(". ").trim()
                            val moves = movePart.split(" ")

                            // 백 이동 추가
                            if (moves.isNotEmpty()) add(moves[0])
                            // 흑 이동 추가
                            if (moves.size > 1) add(moves[1])
                        }
                    }
                }
            }

            // 지정된 인덱스가 범위를 벗어나면 null 반환
            if (index < 0 || index > allMoves.size) {
                null
            } else {
                // 줄 별로 다시 구성하되, 해당 인덱스의 이동을 백틱으로 감싸기
                var currentMoveIdx = 1
                sanList.joinToString("\n") { line ->
                    val lineBuilder = StringBuilder()
                    val trimmed = line.trim()

                    // 이동 번호 부분 처리 (1. 또는 1...)
                    when {
                        trimmed.contains("...") -> {
                            val prefix = trimmed.substringBefore("... ") + "... "
                            lineBuilder.append(prefix)

                            val move = trimmed.substringAfter("... ").trim()
                            if (currentMoveIdx == index) {
                                lineBuilder.append("`$move`")
                            } else {
                                lineBuilder.append(move)
                            }
                            currentMoveIdx++
                        }

                        trimmed.contains(". ") -> {
                            val prefix = trimmed.substringBefore(". ") + ". "
                            lineBuilder.append(prefix)

                            val movePart = trimmed.substringAfter(". ").trim()
                            val moves = movePart.split(" ")

                            if (moves.isNotEmpty()) {
                                // 백 이동 처리
                                if (currentMoveIdx == index) {
                                    lineBuilder.append("`${moves[0]}`")
                                } else {
                                    lineBuilder.append(moves[0])
                                }
                                currentMoveIdx++

                                // 흑 이동 처리 (있는 경우)
                                if (moves.size > 1) {
                                    lineBuilder.append(" ")
                                    if (currentMoveIdx == index) {
                                        lineBuilder.append("`${moves[1]}`")
                                    } else {
                                        lineBuilder.append(moves[1])
                                    }
                                    currentMoveIdx++
                                }
                            }
                        }

                        else -> lineBuilder.append(trimmed)
                    }

                    lineBuilder.toString()
                }
            }
        }

        val splitedSanList = sanList?.let {
            buildList {
                it.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEach

                    when {
                        trimmed.contains("...") -> {
                            // 1... e5 와 같은 형식 (흑 이동만 있는 경우)
                            val move = trimmed.substringAfter("... ").trim()
                            if (move.isNotEmpty()) {
                                add(move)
                            }
                        }

                        trimmed.contains(". ") -> {
                            // 1. e4 e5 와 같은 형식
                            val movePart = trimmed.substringAfter(". ").trim()
                            val moves = movePart.split(" ")

                            // 백 이동 추가
                            if (moves.isNotEmpty() && moves[0].isNotEmpty()) {
                                add(moves[0])
                            }

                            // 흑 이동 추가
                            if (moves.size > 1 && moves[1].isNotEmpty()) {
                                add(moves[1])
                            }
                        }
                    }
                }
            }
        } ?: emptyList()

        val nowFen = getFen(metadataMap["FEN"]!!, splitedSanList.take(index))

        val isFlip = when (data) {
            "flip" -> !(metadataMap["FLIP"]!!.toBoolean())
            else -> metadataMap["FLIP"]!!.toBoolean()
        }

        val imageUrl = URLBuilder("https://www.chess.com/dynboard")
            .apply {
                parameters["fen"] = nowFen
                parameters["size"] = "3"
                parameters["coordinates"] = "true"
                if (isFlip) parameters["flip"] = "true"
            }
            .toString()

        info { "imageUrl: $imageUrl" }

        Embed {
            title = "FEN Preview"
            image = imageUrl
            description = "FEN: `$nowFen`"
            sanString?.let {
                field {
                    name = "기보"
                    value = it
                }
            }
            footer {
                name =
                    "[INDEX=$index] [FLIP=${isFlip}] [FEN=${metadataMap["FEN"]!!}]"
            }
        }.let { hook.editOriginalEmbeds(it).await() }
    }
}