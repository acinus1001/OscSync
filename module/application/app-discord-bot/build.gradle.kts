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

    implementation(libs.bundles.jda)

    implementation(projects.multiplatformCommon.network)
    implementation(projects.multiplatformCommon.chessUtils)

    implementation(projects.module.common.network)
    implementation(projects.module.common.logger)
    implementation(projects.module.common)

    implementation(projects.module.internal.aiClient)
    implementation(projects.module.internal.chessComClient)
    implementation(projects.module.internal.discordClient)
    implementation(projects.module.internal.smartappClient)
    implementation(projects.module.internal.errorHandler)
    implementation(projects.module.internal.mahjongCalc)

    implementation(projects.module.domain.database)
    implementation(projects.module.domain.webhook)
    implementation(projects.module.domain.discordLogging)
    implementation(projects.module.domain.errorHandlerDiscord)
    implementation(projects.module.domain.smartappUserinfo)
    implementation(projects.module.domain.karaoke)
    implementation(projects.module.domain.aiHandler)
    implementation(projects.module.domain.f1News)
    implementation(projects.module.domain.chessCom)
    implementation(projects.module.domain.inquiry)


    implementation(projects.module.domain.memberAuthentication)
}


tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "dev.kuro9.DiscordBotApplicationKt",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

tasks.register<Copy>("copyDependency") {
    from(configurations.runtimeClasspath.get())
    into("${layout.buildDirectory.get()}/libs/lib")
}

/**
 * front build file copy해 spring static으로 서빙 위한 빌드 태스크
 */
//val frontModule = projects.moduleFront.appDiscordBot
//val frontIdentityPath = frontModule.identityPath
//val frontModuleBuildPath = "${rootProject.projectDir.path}/module-front/${frontModule.name}/build"
//
//tasks.named("build") {
//    println("=======build start $frontIdentityPath======")
//    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
//}
//
//tasks.named("bootRun") {
//    println("=======run start ======")
//    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
//}
//
//tasks.named("processResources") {
//    dependsOn(copyFrontendToBackend)
//}
//
//val copyFrontendToBackend by tasks.registering(Copy::class) {
//    dependsOn("$frontIdentityPath:wasmJsBrowserProductionWebpack")
//    delete("$projectDir/src/main/resources/static")
//    println("======= Checking Frontend Build Directory: $frontModuleBuildPath =======")
//    from("$frontModuleBuildPath/kotlin-webpack/wasmJs/productionExecutable/") {
//        include("**/*")
//    }
//    from("$frontModuleBuildPath/processedResources/wasmJs/main/") {
//        include("**/*")
//    }
//
//    println("=======cp start $projectDir ======")
//
//    into("$projectDir/src/main/resources/static")
//}