package dev.kuro9.domain.member.auth.service

import dev.kuro9.domain.member.auth.enumurate.MemberRole
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.domain.member.auth.repository.Members
import dev.kuro9.multiplatform.common.date.util.now
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscordOAuth2UserService : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): DiscordUserDetail {

        val registrationId = userRequest.clientRegistration.registrationId  // discord
        check(registrationId == "discord") {
            "not supported registration id: $registrationId"
        }

        val userAttrName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        @Suppress("UNCHECKED_CAST")
        val userAttr = super.loadUser(userRequest).attributes[userAttrName] as Map<String, Any>

        val memberResultRow = Members.upsertReturning(
            onUpdateExclude = listOf(Members.role, Members.createdAt)
        ) {
            it[id] = (userAttr["id"]!! as String).toLong()
            it[name] = userAttr["username"]!! as String
            it[avatarUrl] = (userAttr["avatar"]!! as String?)
                ?.let { hash -> "https://cdn.discordapp.com/avatars/${userAttr["id"]!! as String}/$hash.png" }
            it[role] = MemberRole.ROLE_BASIC
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }.single()

        return DiscordUserDetail(
            id = memberResultRow[Members.id].value,
            userName = memberResultRow[Members.name],
            role = memberResultRow[Members.role],
            avatarUrl = memberResultRow[Members.avatarUrl],
            userAttr = userAttr,
        )
    }
}