package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kuro9.module.front.application.homepage.network.CommonApiService
import dev.kuro9.module.front.application.homepage.state.user.UserState
import dev.kuro9.multiplatform.common.types.app.homepage.common.DiscordGuildInfo
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class MahjongState {
    var servers by mutableStateOf(emptyList<DiscordGuildInfo>())

    var searchStartDate by mutableStateOf<LocalDate?>(null)
    var searchEndDate by mutableStateOf<LocalDate?>(null)
    var searchUserId by mutableStateOf<Long?>(null)
    var searchUserName by mutableStateOf("")
    var searchMode by mutableStateOf(SearchMode.NAME)

    enum class SearchMode { NAME, ID }
}

class MahjongViewModel(
    private val commonApiService: CommonApiService,
    private val userState: UserState
) : ViewModel() {
    val state = MahjongState()

    fun updateServers() {
        val guildIds = userState.userInfo?.authorities
            ?.filter { it.startsWith("AUTHORITY_HOMEPAGE_MAHJONG-GUILD_") }
            ?.mapNotNull { it.substringAfter("AUTHORITY_HOMEPAGE_MAHJONG-GUILD_").toLongOrNull() }
            ?: return

        if (guildIds.isEmpty()) {
            state.servers = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val servers = commonApiService.getBulkGuildInfo(guildIds)
                state.servers = servers
            } catch (e: Exception) {
                // TODO: error handling
                println("Failed to update mahjong servers: ${e.message}")
            }
        }
    }
}
