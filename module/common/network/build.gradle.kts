plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.retrofit2)
    api(libs.kotlin.reflect)
    api(projects.module.common.serialization)

    implementation(libs.retrofit2.kotlin.serialization.converter)
    implementation(projects.module.common.logger)
}