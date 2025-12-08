plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.module.common.logger)

    // implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.batch)
    implementation(libs.navercorp.spring.batch.plus)
    implementation(libs.spring.boot.starter.aop)

    runtimeOnly(libs.aspectj.runtime)
    runtimeOnly(libs.aspectj.weaver)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.serialization)
    implementation(projects.multiplatformCommon.date)

    implementation(projects.module.common.logger)
    implementation(projects.module.domain.database)
    implementation(projects.module.domain.webhook)

    implementation(projects.module.domain.karaoke)
    implementation(projects.module.domain.f1News)
    implementation(projects.module.domain.chessCom)

    implementation(projects.module.internal.aiClient)
    implementation(projects.module.internal.chessComClient)
}