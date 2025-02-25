plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(projects.module.common.logger)
    implementation(projects.module.internal.oscPublisher)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
}