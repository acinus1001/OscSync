plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.datetime)

    implementation(projects.module.common.logger)
}