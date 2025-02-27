plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.kotlinx.datetime)

    implementation(projects.module.common.logger)
    implementation(projects.module.common.network)

    api(libs.bundles.jda)
}