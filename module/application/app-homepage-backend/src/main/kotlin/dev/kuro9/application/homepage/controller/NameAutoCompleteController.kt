package dev.kuro9.application.homepage.controller

import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordIdAndName
import io.github.harryjhin.slf4j.extension.info
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/names/search")
class NameAutoCompleteController(
    private val discordSearchService: DiscordSearchService,
) {

    @GetMapping
    fun searchNames(@RequestParam keyword: String): List<DiscordIdAndName> {
        return discordSearchService.findByUsername(keyword, limit = 10)
            .map { DiscordIdAndName(it.id, it.name) }
            .also {
                info { "search: $keyword" }
                info { "result: $it" }
            }
    }
}