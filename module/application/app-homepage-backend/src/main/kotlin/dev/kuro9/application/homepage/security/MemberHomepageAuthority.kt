package dev.kuro9.application.homepage.security

import dev.kuro9.domain.member.auth.enumurate.MemberAuthority

sealed class MemberHomepageAuthority : MemberAuthority() {
    override val serviceName = "HOMEPAGE"

    object Mahjong : MemberHomepageAuthority() {
        override val authorityName = "MAHJONG"
    }

    data class MahjongGuild(val guildId: Long) : MemberHomepageAuthority() {
        override val authorityName = "MAHJONG-GUILD"
        override fun toString() = "${super.toString()}_$guildId"

        companion object {
            fun getPrefix(): String = "${super.toString()}_"
            fun parseOrNull(str: String): MahjongGuild? {
                if (!str.startsWith(getPrefix())) return null
                val guildId = str.removePrefix(getPrefix()).toLongOrNull() ?: return null
                return MahjongGuild(guildId)
            }
        }
    }

    object Iot : MemberHomepageAuthority() {
        override val authorityName = "IOT"
    }

    object Vr : MemberHomepageAuthority() {
        override val authorityName = "VR"
    }

    data class User(val userId: Long) : MemberHomepageAuthority() {
        override val authorityName = "USER"
        override fun toString() = "${super.toString()}_$userId"
        fun getPrefix(): String = "${super.toString()}_"
    }
}