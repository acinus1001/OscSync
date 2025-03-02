plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(projects.module.common.logger)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.module.common.network)
    implementation(projects.module.internal.smartappClient)
    implementation(projects.module.internal.discordClient)
    implementation(projects.module.domain.database)
    implementation(projects.module.domain.smartappUserinfo)
}