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
    implementation(libs.kotlin.serialization)

    implementation(projects.multiplatformCommon.serialization)
    implementation(projects.multiplatformCommon.date)

    implementation(projects.module.common)
    implementation(projects.module.common.logger)
    implementation(projects.module.internal.aiClient)
    implementation(projects.module.domain.database)
    implementation(projects.module.domain.cache)
}