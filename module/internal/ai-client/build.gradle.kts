plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)

    implementation(projects.module.common.logger)
    implementation(projects.multiplatformCommon.serialization)

    api(libs.google.ai)
}