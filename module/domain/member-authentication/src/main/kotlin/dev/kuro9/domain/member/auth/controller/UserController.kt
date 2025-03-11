package dev.kuro9.domain.member.auth.controller

import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getUserInfo(
        @AuthenticationPrincipal userInfo: DiscordUserDetail,
    ): UserInfoApiResponse {
        return UserInfoApiResponse(
            userId = userInfo.id,
            userName = userInfo.username,
            userAvatarUrl = null, // todo?
        )
    }
}