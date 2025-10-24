plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.kotlin.serialization)

    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.serialization)
    implementation(projects.multiplatformCommon.date)
    implementation(projects.module.common.logger)
    implementation(projects.module.common)
    implementation(projects.module.domain.database)
    implementation("io.ktor:ktor-client-logging:3.1.1")
}