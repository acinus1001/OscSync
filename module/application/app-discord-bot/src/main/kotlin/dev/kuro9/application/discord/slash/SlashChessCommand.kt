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
import dev.kuro9.internal.chess.api.exception.ChessApiFailureException
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashChessCommand(
    private val chessUserService: ChessComUserService,
    private val chessUserProfileService: ChessComUserProfileService,
    private val discordUserService: DiscordUserNameService,
) : SlashCommandComponent {

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
            prefix = "```\n[등수] 닉네임 ( chess.com 닉네임 )\n",
            postfix = "\n```"
        ) { userInfo: ChessComGuildRank.UserInfo ->
            "[${userInfo.guildRank}] ${nameMap[userInfo.userId]} ( ${userInfo.chessComUserName} )"
        }

        Embed {
            title = "${providedType.displayName} 레이팅"
            description = leaderBoardString
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed = when (t) {

        is NotImplementedError -> Embed {
            title = "Not Implemented"
            description = "This command is not implemented. Contact <@400579163959853056> to report."
            color = Color.RED.rgb
        }

        is ChessApiFailureException -> Embed {
            title = "chess.com API Failure"
            description = "Try again later. If the problem persists, contact <@400579163959853056> to report."
            color = Color.RED.rgb
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

        else -> throw t
    }
}