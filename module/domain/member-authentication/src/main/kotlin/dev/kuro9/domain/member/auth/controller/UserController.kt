package dev.kuro9.domain.member.auth.controller

import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController {

    @GetMapping("/me")
    fun getMyInfo(
        @AuthenticationPrincipal userInfo: DiscordUserDetail,
    ): UserInfoApiResponse {
        return UserInfoApiResponse(
            userId = userInfo.id,
            userName = userInfo.username,
            userAvatarUrl = userInfo.avatarUrl,
            authorities = userInfo.getAuthorities().map { it.authority },
        )
    }
}