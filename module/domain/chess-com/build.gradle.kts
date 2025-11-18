plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(projects.module.domain.database)
    implementation(projects.module.internal.chessComClient)
    implementation(projects.multiplatformCommon.date)
}