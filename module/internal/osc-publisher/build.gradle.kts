plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.module.common)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.java.osc)
}