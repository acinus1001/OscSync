package dev.kuro9.application.homepage.mahjong.controller

import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.kuro9.domain.mahjong.core.service.MahjongRankService
import dev.kuro9.domain.mahjong.core.service.MahjongStatService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Transactional(readOnly = true)
@RestController
@RequestMapping("/services/mahjong/guilds/{guildId}/stats")
class MahjongStatController(
    private val statService: MahjongStatService,
    private val rankService: MahjongRankService,
    private val discordSearchService: DiscordSearchService,
) {
}