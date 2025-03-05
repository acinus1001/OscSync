package dev.kuro9.domain.member.auth.service

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.repository.Members
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.upsertReturning
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscordOAuth2UserService : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val userAttr = super.loadUser(userRequest).attributes

        val registrationId = userRequest.clientRegistration.registrationId  // discord
        check(registrationId == "discord") {
            "not supported registration id: $registrationId"
        }

        val userAttrName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val memberResultRow = Members.upsertReturning(
            onUpdateExclude = listOf(Members.role, Members.createdAt)
        ) {
            it[Members.id] = (userAttr[userAttrName]!! as String).toLong()
            it[Members.name] = userAttr["name"]!! as String
            it[Members.role] = MemberRole.BASIC
            it[Members.createdAt] = LocalDateTime.now()
            it[Members.updatedAt] = LocalDateTime.now()
        }.single()

        return object : OAuth2User {
            override fun getAttributes(): Map<String, Any> = userAttr

            override fun getAuthorities(): Collection<GrantedAuthority> {
                return listOf(SimpleGrantedAuthority(MemberRole.BASIC.toString()))
            }

            override fun getName(): String = memberResultRow[Members.id].toString()

        }
    }
}