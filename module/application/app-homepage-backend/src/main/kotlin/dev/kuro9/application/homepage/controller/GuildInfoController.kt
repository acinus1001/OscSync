package dev.kuro9.application.homepage.controller

import dev.kuro9.application.homepage.security.MemberHomepageAuthority
import dev.kuro9.application.homepage.service.GuildInfoService
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import io.github.harryjhin.slf4j.extension.info
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/common/info/guilds")
class GuildInfoController(
    private val service: GuildInfoService,
) {

    @PostMapping("/bulk")
    fun getBulkGuildInfo(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @RequestBody guildIdList: List<Long>
    ): List<DiscordGuildInfo> {
        // check authorities
        val guildIdListOfHasPermission = guildIdList.filter { guildId ->
            user.hasAllPermissionOf(MemberHomepageAuthority.MahjongGuild(guildId).toString())
        }

        info { "getBulkGuildInfo: 요청=$guildIdList 필터링된 요청=$guildIdListOfHasPermission" }

        if (guildIdListOfHasPermission.isEmpty()) {
            return emptyList()
        }

        return service.getGuildInfo(userId = user.id, *guildIdListOfHasPermission.toLongArray())
    }
}