plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)

    implementation(projects.module.common.logger)
    implementation(projects.module.domain.database)
    implementation(projects.multiplatformCommon.serialization)
    implementation(projects.multiplatformCommon.date)
    implementation(projects.multiplatformCommon.types.member)
}