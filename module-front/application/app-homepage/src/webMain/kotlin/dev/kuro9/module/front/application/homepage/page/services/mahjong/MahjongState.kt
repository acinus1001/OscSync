package dev.kuro9.module.front.application.homepage.page.services.mahjong

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.datetime.LocalDate

data class MahjongServerInfo(val id: String, val name: String, val iconUrl: String?)

class MahjongState {
    var servers by mutableStateOf(
        listOf(
            MahjongServerInfo(
                id = "588993828309041153",
                name = "test",
                iconUrl = null
            )
        )
    )

    var searchStartDate by mutableStateOf<LocalDate?>(null)
    var searchEndDate by mutableStateOf<LocalDate?>(null)
    var searchUserId by mutableStateOf<Long?>(null)
    var searchUserName by mutableStateOf("")
    var searchMode by mutableStateOf(SearchMode.NAME)

    enum class SearchMode { NAME, ID }
}

class MahjongViewModel : ViewModel() {
    val state = MahjongState()
}
