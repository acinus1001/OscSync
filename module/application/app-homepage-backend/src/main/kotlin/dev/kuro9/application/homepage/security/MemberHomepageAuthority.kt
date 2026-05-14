package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.enumurate.MemberAuthority

sealed class MemberHomepageAuthority : MemberAuthority() {
    override val serviceName = "HOMEPAGE"

    object Mahjong : MemberHomepageAuthority() {
        override val authorityName = "MAHJONG"
    }

    object Iot : MemberHomepageAuthority() {
        override val authorityName = "IOT"
    }

    data class User(val userId: Long) : MemberHomepageAuthority() {
        override val authorityName = "USER"
        override fun toString() = "${super.toString()}_$userId"
    }
}