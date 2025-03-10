package dev.kuro9.module.front.discord.app.component.user.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.kuro9.module.front.discord.app.component.user.database.UserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore.*
import dev.kuro9.module.front.discord.app.coroutines.IoCoroutineContext
import dev.kuro9.module.front.discord.app.coroutines.MainCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal fun StoreFactory.userInfoStore(
    database: UserInfoDatabase,
    mainContext: CoroutineContext,
    ioContext: CoroutineContext,
): UserInfoStore = object : UserInfoStore, Store<Intent, State, Label> by create(
    name = UserInfoStore::class.simpleName!!,
    initialState = State(userInfo = null),
    bootstrapper = SimpleBootstrapper(Action.Init),
    executorFactory = {
        ExecutorImpl(
            database = database,
            mainContext = MainCoroutineContext,
            ioContext = IoCoroutineContext,
        )
    },
    reducer = { reduce(it) }
) {}

private sealed interface Action {
    data object Init : Action
    data object Login : Action {
        val url = "/login/todo"
    }

    data object Logout : Action
}

private sealed interface Msg {
    data class UserLoaded(val userInfo: State.UserInfo) : Msg
    data object UserDeleted : Msg
}

private class ExecutorImpl(
    private val database: UserInfoDatabase,
    mainContext: CoroutineContext,
    private val ioContext: CoroutineContext,
) : CoroutineExecutor<Intent, Action, State, Msg, Label>(mainContext) {
    override fun executeAction(action: Action) {
        when (action) {
            Action.Init -> init()
            Action.Login -> scope.launch {
                publish(Label.Redirect(Action.Login.url))
            }

            Action.Logout -> {
                scope.launch { dispatch(Msg.UserDeleted) }
            }
        }
    }

    override fun executeIntent(intent: Intent) {
        when (intent) {
            is Intent.SetUserInfo -> dispatch(Msg.UserLoaded(intent.user))
            Intent.DeleteUserInfo -> dispatch(Msg.UserDeleted)
        }
    }

    private fun init() {
        scope.launch {
            withContext(ioContext) { database.getUserInfo() }
                ?.let {
                    State.UserInfo(
                        id = it.userId,
                        name = it.userName,
                        avatarUrl = it.userAvatarUrl,
                    )
                }
                ?.let { dispatch(Msg.UserLoaded(it)) }
                ?: dispatch(Msg.UserDeleted)
        }
    }
}

private fun State.reduce(msg: Msg): State = when (msg) {
    is Msg.UserLoaded -> copy(userInfo = msg.userInfo)
    Msg.UserDeleted -> copy(userInfo = null)
}
