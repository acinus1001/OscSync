plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.redis)

    implementation(projects.module.domain.cache)
    implementation(projects.multiplatformCommon.strings)
}