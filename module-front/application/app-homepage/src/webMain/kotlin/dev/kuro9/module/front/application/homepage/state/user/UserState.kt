package dev.kuro9.module.front.application.homepage.state.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse

class UserState {
    var userInfo: UserInfoApiResponse? by mutableStateOf(null)
}