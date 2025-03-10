package dev.kuro9.module.front.discord.app

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.kuro9.module.front.discord.app.component.user.database.NetworkUserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.database.UserInfoDatabase
import dev.kuro9.module.front.discord.app.component.user.store.UserInfoStore
import dev.kuro9.module.front.discord.app.component.user.store.userInfoStore
import dev.kuro9.module.front.discord.app.global.GlobalStores
import dev.kuro9.module.front.discord.app.page.main.MainScreenComponent
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // store factory
    single<StoreFactory> {
        LoggingStoreFactory(DefaultStoreFactory())
    }

    // store
    singleOf(::GlobalStores) // 애플리케이션 전역에서 사용되는 store. 파괴되어서는 안 됨.
    factory<UserInfoStore> { DefaultStoreFactory().userInfoStore(get(), get(), get()) }

    // database
    singleOf<UserInfoDatabase>(::NetworkUserInfoDatabase)

    // component
    singleOf(::MainScreenComponent)
}