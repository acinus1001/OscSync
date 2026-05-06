package dev.kuro9.internal.discord.api.resource

import io.ktor.resources.*

@Resource("/api/v10")
class DiscordApiResource {

    @Resource("/guilds/{guildId}")
    class Guild(val guildId: Long, val parent: DiscordApiResource = DiscordApiResource()) {

        @Resource("/members/{userId}")
        class Member(val guildId: Long, val userId: Long, val parent: Guild = Guild(guildId))
    }
}