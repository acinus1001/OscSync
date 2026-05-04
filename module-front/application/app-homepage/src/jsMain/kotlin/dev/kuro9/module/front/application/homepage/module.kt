package dev.kuro9.module.front.application.homepage

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val module = module {
    singleOf(::RouteState)
}