package dev.kuro9.module.front.application.homepage

import dev.kuro9.module.front.application.homepage.network.AuthResourceApiService
import dev.kuro9.module.front.application.homepage.network.AuthResourceManageApiService
import dev.kuro9.module.front.application.homepage.network.IotApiService
import dev.kuro9.module.front.application.homepage.network.MahjongApiService
import dev.kuro9.module.front.application.homepage.state.route.RouteViewModel
import dev.kuro9.module.front.application.homepage.state.user.UserState
import dev.kuro9.module.front.application.homepage.state.user.UserViewModel
import dev.kuro9.module.front.internal.member.service.MemberApiService
import dev.kuro9.multiplatform.common.network.ServerInfo
import dev.kuro9.multiplatform.common.network.getServerInfo
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val module = module {
    single<ServerInfo> { getServerInfo() }
    single<MemberApiService> { MemberApiService(get<ServerInfo>().host, get<ServerInfo>().port) }

    singleOf(::RouteViewModel)
    singleOf(::UserState)
    singleOf(::UserViewModel)
    singleOf(::IotApiService)
    singleOf(::AuthResourceApiService)
    singleOf(::AuthResourceManageApiService)
    singleOf(::MahjongApiService)
}