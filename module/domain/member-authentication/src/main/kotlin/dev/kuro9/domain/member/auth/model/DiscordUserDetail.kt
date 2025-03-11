package dev.kuro9.domain.member.auth.model

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

data class DiscordUserDetail(
    val id: Long,
    val userName: String,
    val role: MemberRole,
    val avatarUrl: String?,

    val userAttr: Map<String, Any?>
) : UserDetails, OAuth2User {
    override fun getAttributes() = userAttr

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword() = ""
    override fun getUsername() = userName
    override fun getName() = id.toString()
}