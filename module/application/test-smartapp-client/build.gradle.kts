plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.module.common)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.module.common.network)
    implementation(projects.module.internal.smartappClient)
}