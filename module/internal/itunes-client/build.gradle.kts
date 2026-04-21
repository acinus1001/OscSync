plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter)

    implementation(projects.module.common.logger)
    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.serialization)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.ktor.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.kotlinx.json)
}