package dev.kuro9.internal.discord.api.resource

import io.ktor.resources.*

@Resource("/api/v10")
class DiscordApiResource {

    @Resource("guilds/{guildId}")
    class Guild(
        val guildId: Long,
        val parent: DiscordApiResource = DiscordApiResource(),
    ) {

        @Resource("members/{userId}")
        class Member(
            val parent: Guild,
            val userId: Long,
        ) {
            constructor(guildId: Long, userId: Long) : this(
                parent = Guild(guildId = guildId),
                userId = userId,
            )
        }
    }

    @Resource("users")
    class User(
        val parent: DiscordApiResource = DiscordApiResource(),
    ) {
        @Resource("@me")
        class Me(val parent: User = User()) {
            @Resource("guilds")
            class Guilds(
                val parent: Me = Me(),
                val before: String? = null,
                val after: String? = null,
                val limit: Int = 200, // [1, 200]
            )
        }
    }
}