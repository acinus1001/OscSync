plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.module.common.logger)
    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.serialization)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

//    implementation(projects.module.domain.cache)
    implementation(projects.module.internal.itunesClient)
    implementation(projects.module.internal.musicCliHandler)

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
}