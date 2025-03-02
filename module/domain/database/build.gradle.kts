plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(libs.jdbc.postgres)
    api(libs.spring.boot.starter.jdbc)
    api(libs.bundles.exposed)
}