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
    val authorities: List<String>,
    val avatarUrl: String?,

    val userAttr: Map<String, Any?>
) : UserDetails, OAuth2User {
    fun hasAnyPermissionOf(vararg authority: String): Boolean {
        if (role == MemberRole.ROLE_ROOT) return true

        return (authorities + role.name).intersect(authorities.toSet()).isNotEmpty()
    }

    fun hasAllPermissionOf(vararg authority: String): Boolean {
        if (role == MemberRole.ROLE_ROOT) return true

        return (authorities + role.name).containsAll(authority.toSet())
    }

    override fun getAttributes() = userAttr

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name)) + authorities.map { SimpleGrantedAuthority(it) }
    }

    override fun getPassword() = ""
    override fun getUsername() = userName
    override fun getName() = id.toString()
}