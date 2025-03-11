package dev.kuro9.domain.member.auth.converter

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtUserInfoConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        val userId = source.subject.toLong()
        val userName = source.getClaim<String>("name")
        val role = source.getClaim<List<MemberRole>>("scp").single()
        val avatarUrl = source.getClaim<String?>("avatar_url")

        val discordUserDetails = DiscordUserDetail(
            id = userId,
            userName = userName,
            role = role,
            avatarUrl = avatarUrl,
            userAttr = source.claims
        )

        return UsernamePasswordAuthenticationToken(
            discordUserDetails,
            source,
            listOf<GrantedAuthority>(SimpleGrantedAuthority(role.name)),
        )
    }
}