plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.module.common)
    implementation(projects.module.internal.oscPublisher)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
}