package dev.kuro9.module.front.discord.app.component.user.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.kuro9.module.front.discord.app.component.user.database.UserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore.*
import io.ktor.util.logging.*
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
            mainContext = mainContext,
            ioContext = ioContext,
        )
    },
    reducer = { reduce(it) }
) {}

private sealed interface Action {
    data object Init : Action
    data object Login : Action {
        val url = "/oauth2/authorization/discord"
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
                KtorSimpleLogger("executor").info("login clicked")
                publish(Label.Redirect(Action.Login.url))
            }

            Action.Logout -> {
                scope.launch {
                    withContext(ioContext) { database.deleteUserInfo() }
                    dispatch(Msg.UserDeleted)
                }
            }
        }
    }

    override fun executeIntent(intent: Intent) {
        when (intent) {
            is Intent.SetUserInfo -> dispatch(Msg.UserLoaded(intent.user))
            Intent.Logout -> forward(Action.Logout)
            Intent.GoLogin -> forward(Action.Login)
        }
    }

    private fun init() {
        KtorSimpleLogger("executor").info("init")
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
