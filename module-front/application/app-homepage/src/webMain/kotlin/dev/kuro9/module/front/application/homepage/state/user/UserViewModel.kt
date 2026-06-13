package dev.kuro9.module.front.application.homepage.state.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.internal.member.exception.MemberApiException
import dev.kuro9.module.front.internal.member.service.MemberApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.types.member.UserInfoApiResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val serverInfo: ServerInfo,
    private val userState: UserState,
    private val memberApiService: MemberApiService,
    private val tokenRefreshService: TokenRefreshService,
) : ViewModel() {
    val effect: SharedFlow<UserEffect>
        field = MutableSharedFlow<UserEffect>()

    fun doLogin() {
        println("doLogin")
        val redirectUrl =
            "${serverInfo.protocol.name}://${serverInfo.host}:${serverInfo.port}/oauth2/authorization/discord"
        viewModelScope.launch {
            effect.emit(UserEffect.OpenOAuth(redirectUrl))
        }
        println("emit")
    }

    suspend fun refreshMyInfo() {
        userState.isLoaded = false
        runCatching {
            // 일반적인 요청
            userState.userInfo = memberApiService.getMyInfo()
            userState.isLoaded = true
        }
            .recoverCatching { // 요청 실패 시
                if (it !is MemberApiException.Unauthorized) throw it
                if (userState.userInfo == null) {
                    // 원래 로그아웃 상태
                    println("not in refresh condition. skipping.")
                    userState.isLoaded = true
                    return@recoverCatching null
                }

                // 토큰 리프레시 실행
                val refreshResult = tokenRefreshService.tryRefresh()
                if (!refreshResult) {
                    println("refresh failed")
                    onRefreshFailure()
                    throw it
                }
                println("refresh success")
                val info = memberApiService.getMyInfo()
                onRefreshSuccess(info)
            }.onFailure {
                println("getMyInfo failed: ${it.message}")
                userState.userInfo = null
                userState.isLoaded = true
            }

    }

    suspend fun doLogout() {
        memberApiService.logout()
        userState.userInfo = null
        userState.isLoaded = true
        viewModelScope.launch {
            effect.emit(UserEffect.Logout)
        }
    }

    fun onRefreshSuccess(info: UserInfoApiResponse? = null) {
        viewModelScope.launch {
            if (info != null) {
                // null 시 다른 요청에서 처리된 상황
                userState.userInfo = info
                userState.isLoaded = true
            }
            effect.emit(UserEffect.RefreshSuccess)
        }
    }

    fun onRefreshFailure() {
        viewModelScope.launch {
            doLogout()
        }
    }
}