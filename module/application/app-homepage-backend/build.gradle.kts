plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.module.common.logger)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)

    runtimeOnly(libs.aspectj.runtime)
    runtimeOnly(libs.aspectj.weaver)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)


    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.date)
    implementation(projects.multiplatformCommon.types.discordApi)
    implementation(projects.multiplatformCommon.types.smartthings)
    implementation(projects.multiplatformCommon.types.appHomepage)

    implementation(projects.module.common.network)
    implementation(projects.module.common.logger)
    implementation(projects.module.common)

    implementation(projects.module.internal.errorHandler)
    implementation(projects.module.internal.discordApi)

    implementation(projects.module.domain.database)
    implementation(projects.module.domain.cache)
    implementation(projects.module.domain.memberAuthentication)
    implementation(projects.module.domain.inquiry)
    implementation(projects.module.domain.smartappUserinfo)
    implementation(projects.module.domain.smartappWebhook)
    implementation(projects.module.domain.mahjongRank)
}