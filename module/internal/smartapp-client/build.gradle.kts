plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.spring.boot.starter.web)

    implementation(projects.module.common)
    implementation(projects.module.common.network)
}