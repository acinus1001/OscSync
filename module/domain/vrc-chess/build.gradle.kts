plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation(projects.module.common.logger)
    implementation(projects.multiplatformCommon.serialization)
    implementation(projects.multiplatformCommon.date)

    implementation(projects.module.internal.chessEngine)
    implementation(projects.module.domain.database)
}