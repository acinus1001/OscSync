package dev.kuro9.module.front.application.homepage.state.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kuro9.module.front.application.homepage.network.common.TokenRefreshService
import dev.kuro9.module.front.internal.member.exception.MemberApiException
import dev.kuro9.module.front.internal.member.service.MemberApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
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
        userState.userInfo = runCatching { memberApiService.getMyInfo() }
            .recoverCatching {
                if (it !is MemberApiException.Unauthorized) throw it

                val refreshResult = tokenRefreshService.tryRefresh()
                if (!refreshResult) {
                    println("refresh failed")
                    throw it
                }
                println("refresh success")
                memberApiService.getMyInfo()
            }
            .fold(
                onSuccess = { it },
                onFailure = { println("getMyInfo failed: ${it.message}"); null }
            )

        userState.isLoaded = true
    }

    suspend fun doLogout() {
        memberApiService.logout()
        userState.userInfo = null
        userState.isLoaded = true
        viewModelScope.launch {
            effect.emit(UserEffect.Logout)
        }
    }
}