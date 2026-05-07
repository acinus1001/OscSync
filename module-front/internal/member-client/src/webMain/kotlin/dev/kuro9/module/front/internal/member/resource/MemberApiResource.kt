package dev.kuro9.module.front.internal.member.resource

import io.ktor.resources.*

@Resource("/users")
class MemberApiResource {

    @Resource("/me")
    class Me(val parent: MemberApiResource = MemberApiResource())

    @Resource("/me/logout")
    class Logout(val parent: MemberApiResource = MemberApiResource())
}