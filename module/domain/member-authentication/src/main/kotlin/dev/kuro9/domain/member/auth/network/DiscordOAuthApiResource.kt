package dev.kuro9.domain.member.auth.network

import io.ktor.resources.*

@Resource("/api/oauth2")
class DiscordOAuthApiResource {

    @Resource("token")
    class RefreshToken(
        val parent: DiscordOAuthApiResource = DiscordOAuthApiResource(),
    )

    @Resource("token/revoke")
    class RevokeToken(
        val parent: DiscordOAuthApiResource = DiscordOAuthApiResource(),
    )
}