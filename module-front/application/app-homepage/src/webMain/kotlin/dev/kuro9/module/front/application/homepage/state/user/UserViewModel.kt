package dev.kuro9.module.front.application.homepage.state.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kuro9.module.front.internal.member.service.MemberApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val serverInfo: ServerInfo,
    private val userState: UserState,
    private val memberApiService: MemberApiService,
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
        try {
            userState.userInfo = memberApiService.getMyInfo()
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401) {
                userState.userInfo = null
            } else e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            userState.isLoaded = true
        }
    }

    suspend fun doLogout() {
        memberApiService.logout()
        userState.userInfo = null
    }
}