plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.module.common.logger)

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aop)

    runtimeOnly(libs.aspectj.runtime)
    runtimeOnly(libs.aspectj.weaver)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)

    implementation(projects.multiplatformCommon.network)

    implementation(projects.module.common.network)
    implementation(projects.module.common.logger)
    implementation(projects.module.common)

    implementation(projects.module.internal.chessEngine)
    implementation(projects.module.internal.errorHandler)

    implementation(projects.module.domain.database)
    implementation(projects.module.domain.cache)
    implementation(projects.module.domain.webhook)
    implementation(projects.module.domain.vrcChess)

    implementation(projects.multiplatformCommon.chessUtils)
}


tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "dev.kuro9.ChessApiApplicationKt",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

tasks.register<Copy>("copyDependency") {
    from(configurations.runtimeClasspath.get())
    into("${layout.buildDirectory.get()}/libs/lib")
}
