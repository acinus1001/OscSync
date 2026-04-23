package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.util.asyncDeferReply
import dev.kuro9.internal.discord.handler.model.ButtonInteractionHandler
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.itunes.service.ItunesApiService
import dev.kuro9.internal.music.connecter.service.MusicConnectService
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import io.github.harryjhin.slf4j.extension.error
import kotlinx.coroutines.Deferred
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SlashMusicControlCommand(
    private val itunesApiService: ItunesApiService,
    private val musicConnectService: MusicConnectService,
) : SlashCommandComponent, ButtonInteractionHandler {
    private val buttonPrefix = "music_ctr_"
    override val commandData: SlashCommandData = Command("music", "음악 영상공유 컨트롤 관련 명령어") {
        subcommand("now-playing", "현재 재생중인 음악 확인")
        subcommand("queue", "재생목록 확인")
        subcommand("skip", "현재 재생중인 음악 스킵")
        subcommand("pause", "현재 재생중인 음악 일시정지")
        subcommand("resume", "일시정지된 음악 재개")

        subcommand("search", "음악 검색") {
            option<String>("query", "검색어")
        }
        subcommand("add-id", "iTunes ID로 재생목록에 음악 추가") {
            option<Long>("iTunesId", "추가하려는 음악의 iTunes ID")
        }
        subcommand("add-search", "검색결과의 최상단 음악으로 재생목록에 음악 추가") {
            option<String>("query", "검색어")
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply: Deferred<InteractionHook> = event.asyncDeferReply()

        runCatching {
            when (event.subcommandName) {
                "now-playing" -> handleNowPlaying(event, deferReply)
                "queue" -> handleQueue(event, deferReply)
                "skip" -> handleSkip(event, deferReply)
                "pause" -> handlePause(event, deferReply)
                "resume" -> handleResume(event, deferReply)
                "search" -> handleSearch(event, deferReply)
                "add-id" -> handleAddId(event, deferReply)
                "add-search" -> handleAddSearch(event, deferReply)

                else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
            }
        }.onFailure { t ->
            deferReply.await().editOriginalEmbeds(getDefaultExceptionEmbed(t)).await()
            return
        }
    }

    override suspend fun isHandleable(event: ButtonInteractionEvent): Boolean {
        return event.componentId.startsWith(buttonPrefix)
    }

    override suspend fun handleButtonInteraction(event: ButtonInteractionEvent) {
        val deferReply = event.deferReply()

        // 형식 : music_ctr_<buttonId>_<data>
        val (buttonId, data) = event.componentId.removePrefix(buttonPrefix).split("_")
            .also { require(it.size == 2) { "올바른 형식이 아닙니다. buttonId: ${event.componentId}" } }

        when (buttonId) {
            "add" -> handleSearchAddButton(event, deferReply, data)
        }
    }

    private suspend fun handleNowPlaying(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val embed = musicConnectService.getNowPlaying()?.let {
            Embed {
                title = "Now Playing..."

                field {
                    name = it.title
                    value = if (it.album != null) "${it.artist} - ${it.album}" else it.artist
                }

                image = it.imageUrl
            }
        } ?: Embed {
            title = "현재 재생 중 아님"
            description = "music add 등으로 음악을 추가해 주세요."
        }

        deferReply.await().editOriginalEmbeds(embed).await()
    }

    private suspend fun handleQueue(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val queue = musicConnectService.getPlayQueue()
        Embed {
            title = "현재 재생 대기 목록"
            description = "큐 길이 : ${queue.size} 개 ( 25개 이상은 전체 표기되지 않습니다. )"
            for (music in queue.take(25)) {
                field {
                    name = music.title
                    value = if (music.album != null) "${music.artist} - ${music.album}" else music.artist
                }
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun handleSkip(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        musicConnectService.skipMusic()

        deferReply.await().editOriginalEmbeds(Embed { title = "200 OK" }).await()
    }

    private suspend fun handlePause(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        musicConnectService.pauseMusic()

        deferReply.await().editOriginalEmbeds(Embed { title = "200 OK" }).await()
    }

    private suspend fun handleResume(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        musicConnectService.resumeMusic()

        deferReply.await().editOriginalEmbeds(Embed { title = "200 OK" }).await()
    }

    private suspend fun handleSearch(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val query = event.getOption("query")!!.asString
        val searchResult = itunesApiService.searchMusic(query)

        val embed = Embed {
            title = "검색어 [${query}] 에 대한 검색 결과 : ${searchResult.size} 건"
            description = "상위 5건만 노출"
            image = searchResult.firstOrNull()?.artworkUrl100
            for (result in searchResult.take(5)) {
                field {
                    name = "${result.trackName} - ${result.artistName}"
                    value = "iTunes ID : ${result.trackId}"
                }
            }
        }

        deferReply.await().run {
            editOriginalEmbeds(embed).await()
            editOriginalComponents(
                searchResult.take(5).mapIndexed { i, result ->
                    ActionRow.of(
                        Button.secondary(
                            "${buttonPrefix}add_${result.trackId}",
                            "[${i + 1}] ${result.trackName} - ${result.artistName}"
                        )
                    )
                }
            ).await()
        }
    }

    private suspend fun handleAddId(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val iTunesId = event.getOption("iTunesId")!!.asLong
        val addedMusic = musicConnectService.addPlayQueue(iTunesId)

        Embed {
            title = "음악 추가 완료"
            description = "${addedMusic.title} - ${addedMusic.artist}"
            image = addedMusic.imageUrl
            footer {
                name = "iTunes ID : ${addedMusic.id}"
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun handleAddSearch(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val query = event.getOption("query")!!.asString
        val searchResult = itunesApiService.searchMusic(query).firstOrNull()

        if (searchResult == null) {
            Embed {
                title = "검색 결과 없음"
                description = "검색 결과가 없습니다."
                color = Color.RED.rgb
            }.let { deferReply.await().editOriginalEmbeds(it).await() }
            return
        }

        val addedMusic = musicConnectService.addPlayQueue(searchResult.trackId)

        Embed {
            title = "음악 추가 완료"
            description = "${addedMusic.title} - ${addedMusic.artist}"
            image = addedMusic.imageUrl
            footer {
                name = "iTunes ID : ${addedMusic.id}"
            }
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun handleSearchAddButton(
        event: ButtonInteractionEvent,
        deferReply: ReplyCallbackAction,
        data: String
    ) {
        val iTunesId = data.toLong()
        val addedMusic = musicConnectService.addPlayQueue(iTunesId)

        val embed = Embed {
            title = "음악 추가 완료"
            description = "${addedMusic.title} - ${addedMusic.artist}"
            image = addedMusic.imageUrl
            footer {
                name = "iTunes ID : ${addedMusic.id}"
            }
        }
        deferReply.await().editOriginalEmbeds(embed).await()
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {

            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }.also { error(t) { "handled err" } }
}