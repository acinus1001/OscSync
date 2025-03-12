package dev.kuro9.multiplatform.common.network.discord.app.resources

import io.ktor.resources.*

/**
 * Fetch User Basic Info
 * - Method : [GET](io.ktor.http.HttpMethod.Get)
 */
@Resource("/api/user")
class Users() {

    @Resource("logout")
    class Logout()
}